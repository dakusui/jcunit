package com.github.dakusui.jcunit8.runners.junit4.utils;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.TuplePredicate;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.testsuite.RowConsumer;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import org.junit.*;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.exceptions.TestDefinitionException.parameterWithoutAnnotation;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum InternalUtils {
  ;
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {{
    put(boolean.class, Boolean.class);
    put(byte.class, Byte.class);
    put(char.class, Character.class);
    put(double.class, Double.class);
    put(float.class, Float.class);
    put(int.class, Integer.class);
    put(long.class, Long.class);
    put(short.class, Short.class);
    put(void.class, Void.class);
  }});

  public static List<String> involvedParameters(TestClass testClass) {
    return Stream.of(
        BeforeClass.class,
        BeforeTestCase.class,
        Before.class,
        Test.class,
        After.class,
        AfterTestCase.class,
        AfterClass.class
    ).flatMap(
        annotationClass -> testClass.getAnnotatedMethods(annotationClass).stream()
    ).flatMap(
        method -> getParameterAnnotationsFrom(method, From.class).stream()
    ).map(
        From.class::cast
    ).map(
        From::value
    ).distinct(
    ).collect(
        toList()
    );
  }

  public static Object invokeExplosivelyWithArgumentsFromTestInput(FrameworkMethod method, KeyValuePairs testInput) throws Throwable {
    return method.invokeExplosively(
        testInput.get("@ins"),
        validateArguments(
            method,
            method.getMethod().getParameterTypes(),
            getParameterAnnotationsFrom(method, From.class).stream()
                .map(From::value)
                .map(testInput::get)
                .toArray()
        )
    );
  }

  public static RunAfters createRunAftersForTestInput(Statement statement, List<RowConsumer> afters, Row testInput) {
    return new RunAfters(
        statement,
        afters.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testInput)
        ).collect(toList())
        , null) {

    };
  }

  public static RunBefores createRunBeforesForTestInput(Statement statement, List<RowConsumer> rowConsumers, Row testInput) {
    return new RunBefores(
        statement,
        rowConsumers.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testInput)
        ).collect(toList()),
        null
    );
  }

  public static Function<RowConsumer, FrameworkMethod> frameworkMethodInvokingArgumentsFromTestCase(Row testInput) {
    return each -> {
      try {
        return new FrameworkMethod(InternalUtils.class.getMethod("frameworkMethodInvokingArgumentsFromTestCase", Row.class)) {
          public Object invokeExplosively(final Object target, final Object... params) {
            each.accept(testInput);
            return null;
          }
        };
      } catch (NoSuchMethodException | SecurityException e) {
        throw Checks.wrap(e);
      }
    };
  }

  static public Object[] validateArguments(FrameworkMethod method, Class<?>[] parameterClasses, Object[] argumentValues) {
    // we can assume parameterClasses.length == argumentValues.length
    for (int i = 0; i < argumentValues.length; i++) {
      if (parameterClasses[i].isPrimitive()) {
        if (argumentValues[i] == null || !PRIMITIVE_TO_WRAPPER.get(parameterClasses[i]).isAssignableFrom(argumentValues[i].getClass())) {
          throw new IllegalArgumentException(composeErrorMessageForTypeMismatch(argumentValues[i], method, i));
        }
      } else {
        if (argumentValues[i] != null && !parameterClasses[i].isAssignableFrom(argumentValues[i].getClass())) {
          throw new IllegalArgumentException(composeErrorMessageForTypeMismatch(argumentValues[i], method, i));
        }
      }
    }
    return argumentValues;
  }

  static String composeErrorMessageForTypeMismatch(Object argumentValue, FrameworkMethod method, int parameterIndex) {
    return String.format("'%s' is not compatible with parameter %s of '%s(%s)'",
        argumentValue,
        parameterIndex,
        method.getName(),
        stream(method.getMethod().getParameterTypes())
            .map(Class::getSimpleName)
            .collect(joining(","))
    );
  }

  public static RowConsumer toTupleConsumer(FrameworkMethod method) {
    return new RowConsumer() {
      @Override
      public String getName() {
        return method.getName();
      }

      @Override
      public void accept(Row testInput) {
        try {
          InternalUtils.invokeExplosivelyWithArgumentsFromTestInput(method, testInput);
        } catch (Throwable throwable) {
          throw Checks.wrap(throwable);
        }
      }
    };
  }

  public static TestOracle toTestOracle(FrameworkMethod method, SortedMap<String, TuplePredicate> predicates) {
    return new TestOracle() {
      @Override
      public String getName() {
        return method.getName();
      }

      @Override
      public Predicate<KeyValuePairs> shouldInvoke() {
        return InternalUtils.shouldInvoke(method, predicates);
      }

      @Override
      public Function<KeyValuePairs, Result> when() {
        return new Function<KeyValuePairs, Result>() {
          @Override
          public Result apply(KeyValuePairs tuple) {
            try {
              return Result.returned(InternalUtils.invokeExplosivelyWithArgumentsFromTestInput(method, tuple));
            } catch (Throwable throwable) {
              return Result.thrown(throwable);
            }
          }

          @Override
          public String toString() {
            return method.getName();
          }
        };
      }

      @Override
      public Predicate<Result> then() {
        return new Predicate<Result>() {
          @Override
          public boolean test(Result result) {
            Class<? extends Throwable> expectedExceptionClass = method.getAnnotation(Test.class).expected();
            if (expectedExceptionClass.equals(Test.None.class)) {
              if (Objects.equals(result.exitedWith(), Result.Exit.RETURNING_VALUE))
                return true;
              if (result.value() instanceof Error)
                throw (Error) result.value();
              if (result.value() instanceof RuntimeException)
                throw (RuntimeException) result.value();
              return false;
            } else {
              return expectedExceptionClass.isAssignableFrom(result.value());
            }
          }

          @Override
          public String toString() {
            return String.format("thrown%s", method.getAnnotation(Test.class).expected().getSimpleName());
          }
        };
      }
    };
  }

  public static Predicate<KeyValuePairs> shouldInvoke(FrameworkMethod method, SortedMap<String, TuplePredicate> predicates) {
    return tuple -> {
      //noinspection SimplifiableIfStatement
      if (method.getAnnotation(Given.class) == null)
        return true;
      return NodeUtils.buildPredicate(
          method.getAnnotation(Given.class).value(),
          predicates
      ).test(
          tuple
      );
    };
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map(
            (List<? extends Annotation> annotations) ->
                (A) annotations.stream(

                ).filter(
                    (Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass())
                ).findFirst(

                ).orElseThrow(
                    () -> parameterWithoutAnnotation(
                        format(
                            "%s.%s",
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName()
                        )))
        ).collect(toList());
  }
}
