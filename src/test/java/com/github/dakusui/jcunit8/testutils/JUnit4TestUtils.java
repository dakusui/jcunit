package com.github.dakusui.jcunit8.testutils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.crest.utils.printable.Printable.function;
import static java.lang.String.format;

public enum JUnit4TestUtils {
  ;

  public static Result runClasses(Class<?>... classes) {
    return makePrintable(JUnitCore.runClasses(classes));
  }

  private static String toString(Result result) {
    return format(
        "result=(%s; runs=%s, failures=%s, ignores=%s)",
        result.wasSuccessful(),
        result.getRunCount(),
        result.getFailureCount(),
        result.getIgnoreCount()
    );
  }

  private static Result makePrintable(Result result) {
    return new Result() {
      @Override
      public int getRunCount() {
        return result.getRunCount();
      }

      @Override
      public int getFailureCount() {
        return result.getFailureCount();
      }

      @Override
      public long getRunTime() {
        return result.getRunTime();
      }

      @Override
      public List<Failure> getFailures() {
        return result.getFailures();
      }

      @Override
      public int getIgnoreCount() {
        return result.getIgnoreCount();
      }

      @Override
      public boolean wasSuccessful() {
        return result.wasSuccessful();
      }

      @Override
      public String toString() {
        return JUnit4TestUtils.toString(this);
      }

    };
  }

  public static Function<Result, Integer> funcGetFailureCount() {
    return function("getFailureCount", Result::getFailureCount);
  }

  public static Function<Result, Integer> funcGetIgnoreCount() {
    return function("getIgnoreCount", Result::getIgnoreCount);
  }

  public static Function<Result, Integer> funcGetRunCount() {
    return function("getRunCount", Result::getRunCount);
  }

  public static Function<Result, Boolean> funcWasSuccessful() {
    return function("wasSuccessful", Result::wasSuccessful);
  }
}
