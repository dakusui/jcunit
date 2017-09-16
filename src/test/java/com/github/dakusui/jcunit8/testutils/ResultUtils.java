package com.github.dakusui.jcunit8.testutils;

import org.hamcrest.Matcher;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.crest.core.Printable.function;
import static java.lang.String.format;
import static org.junit.Assert.assertThat;

public enum ResultUtils {
  ;

  public static void validateJUnitResult(Result result, Matcher<Result> matcher) {
    System.out.println(toString(result));
    assertThat(
        makePrintable(result),
        matcher
    );
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
        return ResultUtils.toString(this);
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
