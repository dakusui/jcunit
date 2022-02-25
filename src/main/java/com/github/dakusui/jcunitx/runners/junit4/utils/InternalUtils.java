package com.github.dakusui.jcunitx.runners.junit4.utils;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.utils.Checks;
import com.github.dakusui.jcunitx.runners.core.TestInputPredicate;
import com.github.dakusui.jcunitx.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunitx.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Given;
import com.github.dakusui.jcunitx.testsuite.TestOracle;
import com.github.dakusui.jcunitx.testsuite.TestInputConsumer;
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

import static com.github.dakusui.jcunitx.exceptions.TestDefinitionException.parameterWithoutAnnotation;
import static com.github.dakusui.jcunitx.runners.core.NodeUtils.buildPredicate;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum InternalUtils {
  ;
  private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = unmodifiableMap(new HashMap<Class<?>, Class<?>>() {{
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

  public static Object invokeExplosivelyWithArgumentsFromTestInput(FrameworkMethod method, AArray testInput) throws Throwable {
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

  public static RunAfters createRunAftersForTestInput(Statement statement, List<TestInputConsumer> afters, AArray testInput) {
    return new RunAfters(
        statement,
        afters.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testInput)
        ).collect(toList())
        , null) {

    };
  }

  public static RunBefores createRunBeforesForTestInput(Statement statement, List<TestInputConsumer> testInputConsumers, AArray testInput) {
    return new RunBefores(
        statement,
        testInputConsumers.stream()
            .map(frameworkMethodInvokingArgumentsFromTestCase(testInput))
            .collect(toList()),
        null
    );
  }

  public static Function<TestInputConsumer, FrameworkMethod> frameworkMethodInvokingArgumentsFromTestCase(AArray testInput) {
    return each -> {
      try {
        return new FrameworkMethod(InternalUtils.class.getMethod("frameworkMethodInvokingArgumentsFromTestCase", AArray.class)) {
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

  @SuppressWarnings("unchecked")
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

  public static TestInputConsumer toTupleConsumer(FrameworkMethod method) {
    return new TestInputConsumer() {
      @Override
      public String getName() {
        return method.getName();
      }

      @Override
      public void accept(AArray testInput) {
        try {
          InternalUtils.invokeExplosivelyWithArgumentsFromTestInput(method, testInput);
        } catch (Throwable throwable) {
          throw Checks.wrap(throwable);
        }
      }
    };
  }

  public static TestOracle toTestOracle(FrameworkMethod method, SortedMap<String, TestInputPredicate> predicates) {
    return new TestOracle() {
      @Override
      public String getName() {
        return method.getName();
      }

      @Override
      public Predicate<AArray> shouldInvoke() {
        return InternalUtils.shouldInvoke(method, predicates);
      }

      @Override
      public Function<AArray, Result> when() {
        return new Function<AArray, Result>() {
          @Override
          public Result apply(AArray tuple) {
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

  public static Predicate<AArray> shouldInvoke(FrameworkMethod method, SortedMap<String, TestInputPredicate> predicates) {
    return row -> {
      //noinspection SimplifiableIfStatement
      if (method.getAnnotation(Given.class) == null)
        return true;
      return buildPredicate(method.getAnnotation(Given.class).value(), predicates).test(row);
    };
  }

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map(
            (List<? extends Annotation> annotations) ->
                (A) annotations.stream()
                    .filter((Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass()))
                    .findFirst()
                    .orElseThrow(() -> parameterWithoutAnnotation(formatFrameworkMethodName(method))))
        .collect(toList());
  }

  private static String formatFrameworkMethodName(FrameworkMethod method) {
    return format("%s.%s", method.getDeclaringClass().getCanonicalName(), method.getName());
  }
}
