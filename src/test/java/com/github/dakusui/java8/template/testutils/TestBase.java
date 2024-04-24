package com.github.dakusui.java8.template.testutils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * This is a base class for unit test class.
 * This suppresses `stdout` and `stderr` if an extending test class is executed from the commandline (through `mvn` command, in other words).
 * If it is run from your IDE, such as IntelliJ IDEA, the `System.out.println()`, for instance, prints your string on your `stdout`, as usual.
 *
 * This is done by checking system properties;
 * - `surefire.real.class.path`
 * - `underpitest`
 *
 * If any of them is set, `stdout` and `stderr` will be suppressed.
 *
 * NOTE::
 * `System.setOut` and `System.setErr` are not thread-safe.
 */
public abstract class TestBase {
  @BeforeEach
  public void before() {
    TestUtils.suppressStdOutErrIfUnderPitestOrSurefire();
  }
  
  @AfterEach
  public void after() {
    TestUtils.restoreStdOutErr();
  }
}
