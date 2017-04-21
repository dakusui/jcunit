package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum TestSuiteUtils {
  ;

  public static <T> void validateTestSuite(TestSuite<T> testSuite, Matcher<TestSuite<T>> matcher) {
    testSuite.forEach(
        System.out::println
    );
    assertThat(testSuite, matcher);
  }
}
