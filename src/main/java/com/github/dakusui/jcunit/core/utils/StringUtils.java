package com.github.dakusui.jcunit.core.utils;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

public enum StringUtils {
  ;

  /**
   * Returns a user friendly string using given format and arguments.
   * <p>
   * Should only be used for messages directly shown to users because this method
   * does relatively heavy conversions to keep the returned string concise and
   * informative.
   * Also note that unlike {@code String#format}, only '%s' specifier is supported.
   *
   * @param format A format string.
   * @param args   Arguments referenced by the format specifier in the format string.
   */
  public static String format(String format, Object... args) {
    return String.format(
        checknotnull(format),
        Stream.of(args).map(in -> {
          if (in == null)
            return null;
          //noinspection ConstantConditions (it's already checked)
          Class<?> toStringDeclaringClass = ReflectionUtils.getMethod(in.getClass(), "toString").getDeclaringClass();
          if (Object.class.equals(toStringDeclaringClass)) {
            return toString(in);
          } else if (in instanceof Class) {
            return getSimpleName((Class) in);
          }
          return in.toString();
        }).toArray());
  }

  public static String toString(Object o) {
    if (o == null)
      return null;
    if (o instanceof Class) {
      return getSimpleName((Class) o);
    }
    return getSimpleName(o.getClass()) + "@" + System.identityHashCode(o);
  }

  public static String getSimpleName(Class c) {
    return join("$", composeSimpleName(new LinkedList<String>(), checknotnull(c)).toArray());
  }

  private static List<String> composeSimpleName(List<String> classNest, Class c) {
    if (c == null)
      return classNest;
    classNest.add(0, c.getSimpleName());
    return composeSimpleName(classNest, c.getEnclosingClass());
  }

  /**
   * Joins given string objects with {@code sep} using {@code formatter}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep       A separator to be used to join {@code elemes}.
   * @param formatter A formatter used to join strings.
   * @param elems     Elements to be joined.
   * @return A joined {@code String}
   */
  @SafeVarargs
  public static <T> String join(String sep, Formatter<T> formatter,
      T... elems) {
    return Stream.of(elems)
        .map(formatter::format)
        .collect(Collectors.joining(checknotnull(sep)));
  }

  /**
   * Joins given string objects with {@code sep} using {@code Formatter.CONFIG}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep   A separator to be used to join {@code elemes}
   * @param elems Elements to be joined.
   * @return A joined {@code String}
   */
  public static String join(String sep, Object... elems) {
    //noinspection unchecked
    return join(sep, Formatter.INSTANCE, elems);
  }

  public static String join(String sep, List<?> elems) {
    //noinspection unchecked
    return join(sep, Formatter.INSTANCE, checknotnull(elems).toArray());
  }


  public interface Formatter<T> {
    Formatter INSTANCE = (Formatter<Object>) elem ->
        elem == null ?
            null :
            elem.toString();

    String format(T elem);
  }
}
