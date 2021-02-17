package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.*;

public interface ArgumentConverter {
  ArgumentConverter.Default DEFAULT_INSTANCE = new ArgumentConverter.Default();

  Object convert(Object source, ParameterContext context) throws ArgumentConversionException;

  abstract class Simple implements ArgumentConverter {
    @Override
    public final Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
      return convert(source, context.getParameter().getType());
    }

    /**
     * Convert the supplied {@code source} object into to the supplied
     * {@code targetType}.
     *
     * @param source     the source object to convert; may be {@code null}
     * @param targetType the target type the source object should be converted
     *                   into; never {@code null}
     * @return the converted object; may be {@code null} but only if the target
     * type is a reference type
     * @throws ArgumentConversionException in case an error occurs during the
     *                                     conversion
     */
    protected abstract Object convert(Object source, Class<?> targetType) throws ArgumentConversionException;

  }

  class Default extends Simple {
    private static final List<StringToObjectConverter> stringToObjectConverters = unmodifiableList(asList( //
        new StringToPrimitiveConverter(), //
        new StringToEnumConverter(), //
        new StringToJavaTimeConverter(), //
        new StringToCommonJavaTypesConverter(), //
        new FallbackStringToObjectConverter() //
    ));

    @Override
    public Object convert(Object source, Class<?> targetType) {
      if (source == null) {
        if (targetType.isPrimitive()) {
          throw new ArgumentConversionException(
              "Cannot convert null to primitive value of type " + targetType.getName());
        }
        return null;
      }

      if (ReflectionUtils.isAssignableTo(source, targetType)) {
        return source;
      }

      return convertToTargetType(source, toWrapperType(targetType));
    }

    private Object convertToTargetType(Object source, Class<?> targetType) {
      if (source instanceof String) {
        Optional<StringToObjectConverter> converter = stringToObjectConverters.stream().filter(
            candidate -> candidate.canConvert(targetType)).findFirst();
        if (converter.isPresent()) {
          try {
            return converter.get().convert((String) source, targetType);
          } catch (Exception ex) {
            if (ex instanceof ArgumentConversionException) {
              // simply rethrow it
              throw (ArgumentConversionException) ex;
            }
            // else
            throw new ArgumentConversionException(
                "Failed to convert String \"" + source + "\" to type " + targetType.getName(), ex);
          }
        }
      }
      throw new ArgumentConversionException("No implicit conversion to convert object of type "
          + source.getClass().getName() + " to type " + targetType.getName());
    }

    private static Class<?> toWrapperType(Class<?> targetType) {
      Class<?> wrapperType = getWrapperType(targetType);
      return wrapperType != null ? wrapperType : targetType;
    }
  }

  interface StringToObjectConverter {

    boolean canConvert(Class<?> targetType);

    Object convert(String source, Class<?> targetType) throws Exception;

  }

  class StringToPrimitiveConverter implements StringToObjectConverter {

    private static final Map<Class<?>, Function<String, ?>> CONVERTERS;

    static {
      Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
      converters.put(Boolean.class, Boolean::valueOf);
      converters.put(Character.class, source -> {
        Preconditions.condition(source.length() == 1, () -> "String must have length of 1: " + source);
        return source.charAt(0);
      });
      converters.put(Byte.class, Byte::decode);
      converters.put(Short.class, Short::decode);
      converters.put(Integer.class, Integer::decode);
      converters.put(Long.class, Long::decode);
      converters.put(Float.class, Float::valueOf);
      converters.put(Double.class, Double::valueOf);
      CONVERTERS = unmodifiableMap(converters);
    }

    @Override
    public boolean canConvert(Class<?> targetType) {
      return CONVERTERS.containsKey(targetType);
    }

    @Override
    public Object convert(String source, Class<?> targetType) {
      return CONVERTERS.get(targetType).apply(source);
    }
  }

  class StringToEnumConverter implements StringToObjectConverter {

    @Override
    public boolean canConvert(Class<?> targetType) {
      return targetType.isEnum();
    }

    @Override
    public Object convert(String source, Class<?> targetType) throws Exception {
      return valueOf(targetType, source);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object valueOf(Class targetType, String source) {
      return Enum.valueOf(targetType, source);
    }
  }

  class StringToJavaTimeConverter implements StringToObjectConverter {

    private static final Map<Class<?>, Function<String, ?>> CONVERTERS;

    static {
      Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
      converters.put(Duration.class, Duration::parse);
      converters.put(Instant.class, Instant::parse);
      converters.put(LocalDate.class, LocalDate::parse);
      converters.put(LocalDateTime.class, LocalDateTime::parse);
      converters.put(LocalTime.class, LocalTime::parse);
      converters.put(MonthDay.class, MonthDay::parse);
      converters.put(OffsetDateTime.class, OffsetDateTime::parse);
      converters.put(OffsetTime.class, OffsetTime::parse);
      converters.put(Period.class, Period::parse);
      converters.put(Year.class, Year::parse);
      converters.put(YearMonth.class, YearMonth::parse);
      converters.put(ZonedDateTime.class, ZonedDateTime::parse);
      converters.put(ZoneId.class, ZoneId::of);
      converters.put(ZoneOffset.class, ZoneOffset::of);
      CONVERTERS = Collections.unmodifiableMap(converters);
    }

    @Override
    public boolean canConvert(Class<?> targetType) {
      return CONVERTERS.containsKey(targetType);
    }

    @Override
    public Object convert(String source, Class<?> targetType) throws Exception {
      return CONVERTERS.get(targetType).apply(source);
    }
  }

  class StringToCommonJavaTypesConverter implements StringToObjectConverter {

    private static final Map<Class<?>, Function<String, ?>> CONVERTERS;

    static {
      Map<Class<?>, Function<String, ?>> converters = new HashMap<>();

      // java.lang
      converters.put(Class.class, StringToCommonJavaTypesConverter::toClass);
      // java.io and java.nio
      converters.put(File.class, File::new);
      converters.put(Charset.class, Charset::forName);
      converters.put(Path.class, Paths::get);
      // java.net
      converters.put(URI.class, URI::create);
      converters.put(URL.class, StringToCommonJavaTypesConverter::toURL);
      // java.math
      converters.put(BigDecimal.class, BigDecimal::new);
      converters.put(BigInteger.class, BigInteger::new);
      // java.util
      converters.put(Currency.class, Currency::getInstance);
      converters.put(Locale.class, Locale::new);
      converters.put(UUID.class, UUID::fromString);

      CONVERTERS = Collections.unmodifiableMap(converters);
    }

    @Override
    public boolean canConvert(Class<?> targetType) {
      return CONVERTERS.containsKey(targetType);
    }

    @Override
    public Object convert(String source, Class<?> targetType) throws Exception {
      return CONVERTERS.get(targetType).apply(source);
    }

    private static Class<?> toClass(String type) {
      //@formatter:off
      return ReflectionUtils
          .tryToLoadClass(type)
          .getOrThrow(cause -> new ArgumentConversionException(
              "Failed to convert String \"" + type + "\" to type " + Class.class.getName(), cause));
      //@formatter:on
    }

    private static URL toURL(String url) {
      try {
        return new URL(url);
      } catch (MalformedURLException ex) {
        throw new ArgumentConversionException(
            "Failed to convert String \"" + url + "\" to type " + URL.class.getName(), ex);
      }
    }

  }

  class FallbackStringToObjectConverter implements StringToObjectConverter {

    /**
     * Implementation of the NULL Object Pattern.
     */
    private static final Function<String, Object> NULL_EXECUTABLE = source -> source;

    /**
     * Cache for factory methods and factory constructors.
     *
     * <p>Searches that do not find a factory method or constructor are tracked
     * by the presence of a {@link #NULL_EXECUTABLE} object stored in the map.
     * This prevents the framework from repeatedly searching for things which
     * are already known not to exist.
     */
    private static final ConcurrentHashMap<Class<?>, Function<String, Object>> factoryExecutableCache //
        = new ConcurrentHashMap<>(64);

    @Override
    public boolean canConvert(Class<?> targetType) {
      return findFactoryExecutable(targetType) != NULL_EXECUTABLE;
    }

    @Override
    public Object convert(String source, Class<?> targetType) throws Exception {
      Function<String, Object> executable = findFactoryExecutable(targetType);
      Preconditions.condition(executable != NULL_EXECUTABLE,
          "Illegal state: convert() must not be called if canConvert() returned false");

      return executable.apply(source);
    }

    private static Function<String, Object> findFactoryExecutable(Class<?> targetType) {
      return factoryExecutableCache.computeIfAbsent(targetType, type -> {
        Method factoryMethod = findFactoryMethod(type);
        if (factoryMethod != null) {
          return source -> invokeMethod(factoryMethod, null, source);
        }
        Constructor<?> constructor = findFactoryConstructor(type);
        if (constructor != null) {
          return source -> newInstance(constructor, source);
        }
        return NULL_EXECUTABLE;
      });
    }

    private static Method findFactoryMethod(Class<?> targetType) {
      List<Method> factoryMethods = findMethods(targetType, new IsFactoryMethod(targetType), BOTTOM_UP);
      if (factoryMethods.size() == 1) {
        return factoryMethods.get(0);
      }
      return null;
    }

    private static Constructor<?> findFactoryConstructor(Class<?> targetType) {
      List<Constructor<?>> constructors = findConstructors(targetType, new IsFactoryConstructor(targetType));
      if (constructors.size() == 1) {
        return constructors.get(0);
      }
      return null;
    }

    /**
     * {@link Predicate} that determines if the {@link Method} supplied to
     * {@link #test(Method)} is a non-private static factory method for the
     * supplied {@link #targetType}.
     */
    static class IsFactoryMethod implements Predicate<Method> {

      private final Class<?> targetType;

      IsFactoryMethod(Class<?> targetType) {
        this.targetType = targetType;
      }

      @Override
      public boolean test(Method method) {
        // Please do not collapse the following into a single statement.
        if (!method.getReturnType().equals(this.targetType)) {
          return false;
        }
        if (isNotStatic(method)) {
          return false;
        }
        return isNotPrivateAndAcceptsSingleStringArgument(method);
      }

    }

    /**
     * {@link Predicate} that determines if the {@link Constructor} supplied to
     * {@link #test(Constructor)} is a non-private factory constructor for the
     * supplied {@link #targetType}.
     */
    static class IsFactoryConstructor implements Predicate<Constructor<?>> {

      private final Class<?> targetType;

      IsFactoryConstructor(Class<?> targetType) {
        this.targetType = targetType;
      }

      @Override
      public boolean test(Constructor<?> constructor) {
        // Please do not collapse the following into a single statement.
        if (!constructor.getDeclaringClass().equals(this.targetType)) {
          return false;
        }
        return isNotPrivateAndAcceptsSingleStringArgument(constructor);
      }

    }

    private static boolean isNotPrivateAndAcceptsSingleStringArgument(Executable executable) {
      return isNotPrivate(executable) //
          && (executable.getParameterCount() == 1) //
          && (executable.getParameterTypes()[0] == String.class);
    }

  }

}
