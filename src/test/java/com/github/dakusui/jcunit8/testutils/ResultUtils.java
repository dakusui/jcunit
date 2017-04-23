package com.github.dakusui.jcunit8.testutils;

import org.hamcrest.Matcher;
import org.junit.runner.Result;

import static java.lang.String.format;
import static org.junit.Assert.assertThat;

public enum ResultUtils {
  ;

  public static void validateJUnitResult(Result result, Matcher<Result> matcher) {
    System.out.println(format(
        "result=(%s; runs=%s, failures=%s:%s, ignores=%s)",
        result.wasSuccessful(),
        result.getRunCount(),
        result.getFailureCount(),
        result.getFailures(),
        result.getIgnoreCount()
    ));
    assertThat(
        result,
        matcher
    );
  }
}
