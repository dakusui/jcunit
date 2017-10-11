package com.github.dakusui.jcunit8.runners.junit4.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.ITestCaseRunner;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import com.github.dakusui.jcunit8.testsuite.TupleConsumer;
import org.junit.*;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum InternalUtils {
  ;
  private static final Map<Class, Class> PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(new HashMap<Class, Class>() {{
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
        method -> JCUnit8.getParameterAnnotationsFrom(method, From.class).stream()
    ).map(
        From.class::cast
    ).map(
        From::value
    ).distinct(
    ).collect(
        toList()
    );
  }

  public static Statement createMethodInvoker(FrameworkMethod method, JCUnit8.TestCaseRunner testCaseRunner, Object test) throws Throwable {
    return new InvokeMethod(method, test) {
      @Override
      public void evaluate() throws Throwable {
        invokeExplosivelyWithArgumentsFromTestInput(method, testCaseRunner.getTestCase().getTestInput());
      }
    };
  }

  public static Object invokeExplosivelyWithArgumentsFromTestInput(FrameworkMethod method, Tuple testInput) throws Throwable {
    return method.invokeExplosively(
        testInput.get("@ins"),
        validateArguments(
            method,
            method.getMethod().getParameterTypes(),
            JCUnit8.getParameterAnnotationsFrom(method, From.class).stream()
                .map(From::value)
                .map(testInput::get)
                .collect(toList())
                .toArray()
        )
    );
  }

  public static RunAfters createRunAftersForTestCase_(Statement statement, List<FrameworkMethod> afters, ITestCaseRunner testCaseRunner) {
    return new RunAfters(
        statement,
        afters.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase_(testCaseRunner, null)
        ).collect(toList())
        , null) {

    };
  }


  public static RunAfters createRunAftersForTestInput(Statement statement, List<TupleConsumer> afters, Tuple testInput) {
    return new RunAfters(
        statement,
        afters.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testInput)
        ).collect(toList())
        , null) {

    };
  }

  public static RunBefores createRunBeforesForTestCase_(Statement statement, List<FrameworkMethod> befores, ITestCaseRunner testCaseRunner) {
    return new RunBefores(
        statement,
        befores.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase_(testCaseRunner, null)
        ).collect(toList()),
        null
    );
  }

  public static RunBefores createRunBeforesForTestInput(Statement statement, List<TupleConsumer> tupleConsumers, Tuple testInput) {
    return new RunBefores(
        statement,
        tupleConsumers.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testInput)
        ).collect(toList()),
        null
    );
  }

  public static Function<FrameworkMethod, FrameworkMethod> frameworkMethodInvokingArgumentsFromTestCase_(ITestCaseRunner testCaseRunner, Object test) {
    return each -> new FrameworkMethod(each.getMethod()) {
      public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
        return invokeExplosivelyWithArgumentsFromTestInput(each, testCaseRunner.getTestCase().getTestInput());
      }

    };
  }


  public static Function<TupleConsumer, FrameworkMethod> frameworkMethodInvokingArgumentsFromTestCase(Tuple testInput) {
    return each -> {
      try {
        return new FrameworkMethod(InternalUtils.class.getMethod("frameworkMethodInvokingArgumentsFromTestCase", Tuple.class)) {
          public Object invokeExplosively(final Object target, final Object... params)
              throws Throwable {
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
  static public Object[] validateArguments(FrameworkMethod method, Class[] parameterClasses, Object[] argumentValues) {
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

  public static TupleConsumer toTupleConsumer(FrameworkMethod method) {
    return new TupleConsumer() {
      @Override
      public String getName() {
        return method.getName();
      }

      @Override
      public void accept(Tuple testInput) {
        try {
          InternalUtils.invokeExplosivelyWithArgumentsFromTestInput(method, testInput);
        } catch (Throwable throwable) {
          throw Checks.wrap(throwable);
        }
      }
    };
  }

  public static TestOracle toTestOracle(FrameworkMethod method, SortedMap<String, TestPredicate> predicates) {
    return new TestOracle() {
      @Override
      public Predicate<Tuple> shouldInvoke() {
        return InternalUtils.shouldInvoke(method, predicates);
      }

      @Override
      public Function<Tuple, Result> when() {
        return new Function<Tuple, Result>() {
          @Override
          public Result apply(Tuple tuple) {
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
            return expectedExceptionClass.equals(Test.None.class) ?
                Objects.equals(result.exitedWith(), Result.Exit.RETURNING_VALUE) :
                expectedExceptionClass.isAssignableFrom(result.value());
          }

          @Override
          public String toString() {
            return String.format("then%sThrown", method.getAnnotation(Test.class).expected().getSimpleName());
          }
        };
      }
    };
  }

  public static Predicate<Tuple> shouldInvoke(FrameworkMethod method, SortedMap<String, TestPredicate> predicates) {
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
}
