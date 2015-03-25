package com.github.dakusui.jcunit.metatest;

import com.github.dakusui.jcunit.core.Checks;
import junit.framework.AssertionFailedError;
import org.junit.internal.requests.ClassRequest;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Metatest {
  public static class MetatestAssertionFailedError extends AssertionFailedError {
    public final List<String> violations;

    public MetatestAssertionFailedError(String message, List<String> violations) {
      super(formatMessage(message, violations));
      this.violations = Collections.unmodifiableList(violations);
    }

    private static String formatMessage(String message, List<String> violations) {
      return message + violations;
    }
  }


  public void executeTestMethod(Class<?> testClass, String testMethodName) {
    Checks.checknotnull(testClass);
    Checks.checknotnull(testMethodName);
    composeTestExpectation(testClass, testMethodName).check(runTestMethod(testClass, testMethodName));
  }

  private Expectation composeTestExpectation(Class<?> testClass, String testMethodName) {
    try {
      Method m = testClass.getMethod(testMethodName);
      String testName = String.format("%s#%s", testClass.getCanonicalName(), testMethodName);
      if (m.isAnnotationPresent(Expected.class)) {
        Expected expected = m.getAnnotation(Expected.class);
        return new Expectation(testName, expected);
      } else {
        return new Expectation(testName, defaultExpected);
      }

    } catch (NoSuchMethodException e) {
      Checks.rethrowtesterror(e, String.format("The test '%s#%s' was not found", testClass.getCanonicalName(), testMethodName));
    }
    Checks.checkcond(false); // This path shouldn't be executed.
    return null;
  }

  private Result runTestMethod(final Class<?> testClass, final String methodName) {
    return new JUnitCore().run(composeRequestForTestMethod(testClass, methodName));
  }

  private Request composeRequestForTestMethod(final Class<?> testClass, final String methodName) {
    return new FilterRequest(new ClassRequest(testClass), new Filter() {
      @Override
      public boolean shouldRun(Description description) {
        return methodName.equals(description.getMethodName());
      }

      @Override
      public String describe() {
        return String.format("%s#%s", testClass.getCanonicalName(), methodName);
      }
    });
  }

  public static final Expected defaultExpected = new Expected() {
    @Override
    public boolean passing() {
      return true;
    }

    @Override
    public Class<? extends Throwable> exception() {
      return Throwable.class;
    }

    @Override
    public String messagePattern() {
      return "";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Expected.class;
    }
  };

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Expected {

    public boolean passing() default true;

    public Class<? extends Throwable> exception() default Throwable.class;

    public String messagePattern() default "";
  }

  private class Expectation {
    private final Expected expected;
    private final String   testName;

    public Expectation(String testName, Expected expected) {
      Checks.checknotnull(testName);
      Checks.checknotnull(expected);
      if (expected.passing()) {
        Checks.checktest(
            Throwable.class.equals(expected.exception()) && "".equals(expected.messagePattern()),
            "%s: 'exception' and 'messagePattern' can't be set when 'passing' is set to true.", testName);
      }

      this.testName = testName;
      this.expected = expected;
    }

    public void check(Result result) {
      Checks.checknotnull(result);
      Checks.checkparam(result.getIgnoreCount() == 0);
      Checks.checkparam(result.getRunCount() == 1);
      Checks.checkparam(result.getFailures().size() <= 1);
      Checks.checkparam(result.getFailureCount() <= 1);
      List<String> violations = new ArrayList<String>();

      ////
      // Pass or fail
      if (this.expected.passing()) {
        check(result.wasSuccessful(), "Test was expected to pass but failed", violations);
      } else /* The test should fail */ {
        check(!result.wasSuccessful(), "Test was expected to fail but passed", violations);
        int c = result.getFailureCount();
        check(c == 1, String.format("Failure count was expected to 1 but %d", c), violations);
      }

      ////
      // Exception matches the expectation or not.
      if (!Throwable.class.equals(this.expected.exception())) {
        //noinspection StatementWithEmptyBody
        if (result.getFailures().size() > 0) {
          @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
          Throwable t = result.getFailures().get(0).getException();
          check(this.expected.exception().isAssignableFrom(t.getClass()),
              String.format(
                  "An instance of '%s' was expected to be thrown but '%s' was thrown",
                  this.expected.exception().getCanonicalName(),
                  t.getClass().getCanonicalName()
              ),
              violations
          );
        } else {
          ////
          // Do nothing since the check for the case where we are expecting
          // failure but seeing success is already covered by the previous
          // IF statement. Avoid making the messages redundant.
        }
      }

      ////
      // Message matches the expectation or not
      if (!"".equals(this.expected.messagePattern())) {
        //noinspection StatementWithEmptyBody
        if (result.getFailures().size() > 0) {
          String msg = result.getFailures().get(0).getMessage();
          check(
              msg.matches(this.expected.messagePattern()),
              String.format(
                  "Message should match the pattern: '%s' but didn't. The actual message was '%s'",
                  this.expected.messagePattern(),
                  msg
              ),
              violations
          );
        } else {
          ////
          // For the same reason as the previous if statement, do nothing here.
        }
      }

      if (!violations.isEmpty()) {
        throw new MetatestAssertionFailedError(
            String.format("The test result for '%s' didn't meet the expectation. ", testName),
            violations);
      }
    }

    private void check(boolean cond, String message, List<String> violations) {
      if (!cond) {
        violations.add(message);
      }
    }
  }
}


