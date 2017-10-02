package com.github.dakusui.jcunit8.runners.junit4.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import com.github.dakusui.jcunit8.testsuite.TestScenario;
import org.junit.*;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
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
        invokeExplosivelyWithArgumentsFromTestInput(method, testCaseRunner);
      }
    };
  }

  public static Object invokeExplosivelyWithArgumentsFromTestInput(FrameworkMethod method, JCUnit8.TestCaseRunner testCaseRunner) throws Throwable {
    return invokeExplosivelyWithArgumentsFromTestInput(method, testCaseRunner.getTestCase().get());
  }

  public static Object invokeExplosivelyWithArgumentsFromTestInput(FrameworkMethod method, Tuple testInput) throws Throwable {
    Object[] args = validateArguments(
        method,
        method.getMethod().getParameterTypes(),
        JCUnit8.getParameterAnnotationsFrom(method, From.class).stream()
            .map(From::value)
            .map(testInput::get)
            .collect(toList())
            .toArray()
    );
    return method.invokeExplosively(testInput.get("@ins"), args);
  }

  public static RunAfters createRunAftersForTestCase(Statement statement, List<FrameworkMethod> afters, JCUnit8.TestCaseRunner testCaseRunner) {
    return new RunAfters(
        statement,
        afters.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testCaseRunner, null)
        ).collect(toList())
        , null) {

    };
  }

  public static RunBefores createRunBeforesForTestCase(Statement statement, List<FrameworkMethod> befores, JCUnit8.TestCaseRunner testCaseRunner) {
    return new RunBefores(
        statement,
        befores.stream().map(
            frameworkMethodInvokingArgumentsFromTestCase(testCaseRunner, null)
        ).collect(toList()),
        null
    );
  }


  public static Function<FrameworkMethod, FrameworkMethod> frameworkMethodInvokingArgumentsFromTestCase(JCUnit8.TestCaseRunner testCaseRunner, Object test) {
    return each -> new FrameworkMethod(each.getMethod()) {
      public Object invokeExplosively(final Object target, final Object... params)
          throws Throwable {
        return new ReflectiveCallable() {
          @Override
          protected Object runReflectiveCall() throws Throwable {
            return invokeExplosivelyWithArgumentsFromTestInput(each, testCaseRunner);
          }
        }.run();
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

  static Consumer<Tuple> toTupleConsumer(FrameworkMethod method, Object testObject) {
    return new Consumer<Tuple>() {
      @Override
      public void accept(Tuple tuple) {

      }
    };
  }

  static Runnable toRunnable(FrameworkMethod method, Object testObject) {
    return new Runnable() {
      @Override
      public void run() {

      }
    };
  }

  static TestOracle toTestOracle(FrameworkMethod method, Object testObject) {
    return new TestOracle() {
      @Override
      public boolean shouldInvoke(Tuple tuple) {
        return false;
      }

      @Override
      public String getName() {
        return null;
      }

      @Override
      public Result apply(Tuple tuple) {
        return null;
      }

      @Override
      public boolean test(Result result) {
        return false;
      }
    };
  }

  public static TestScenario creteTestScenario(TestClass testClass, Object obj) {
    TestScenario.Builder builder = new TestScenario.Builder();
    testClass.getAnnotatedMethods(BeforeClass.class).stream().map(
        method -> toRunnable(method, null)
    ).forEach(
        builder::addPreTestSuiteProcedure
    );
    testClass.getAnnotatedMethods(BeforeTestCase.class).stream().map(
        (FrameworkMethod method) -> toTupleConsumer(method, null)
    ).forEach(
        builder::addPreTestCaseProcedure
    );
    testClass.getAnnotatedMethods(Before.class).stream().map(
        (FrameworkMethod method) -> toTupleConsumer(method, obj)
    ).forEach(
        builder::addPreOracleProcedure
    );
    testClass.getAnnotatedMethods(Test.class).stream().map(
        (FrameworkMethod method) -> toTestOracle(method, obj)
    ).forEach(
        builder::addTestOracle
    );
    testClass.getAnnotatedMethods(After.class).stream().map(
        (FrameworkMethod method) -> toTupleConsumer(method, obj)
    ).forEach(
        builder::addPostOracleProcedure
    );
    testClass.getAnnotatedMethods(AfterTestCase.class).stream().map(
        (FrameworkMethod method) -> toTupleConsumer(method, null)
    ).forEach(
        builder::addPostTestCaseProcedure
    );
    testClass.getAnnotatedMethods(AfterClass.class).stream().map(
        method -> toRunnable(method, null)
    ).forEach(
        builder::addPostTestSuiteProcedure
    );
    return builder.build();
  }
}
