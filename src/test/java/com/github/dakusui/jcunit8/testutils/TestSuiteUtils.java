package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum TestSuiteUtils {
  ;

  public static void validateTestSuite(TestSuite testSuite, Matcher<TestSuite> matcher) {
    testSuite.forEach(
        each -> com.github.dakusui.jcunit8.core.Utils.out().println(each)
    );
    assertThat(testSuite, matcher);
  }
}
