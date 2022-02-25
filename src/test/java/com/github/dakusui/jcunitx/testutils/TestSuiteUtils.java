package com.github.dakusui.jcunitx.testutils;

import com.github.dakusui.jcunitx.testsuite.TestSuite;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum TestSuiteUtils {
  ;

  public static void validateTestSuite(TestSuite testSuite, Matcher<TestSuite> matcher) {
    testSuite.forEach(
        System.out::println
    );
    assertThat(testSuite, matcher);
  }
}
