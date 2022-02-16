package com.github.dakusui.jcunit8.runners.junit4;

import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * This interface is necessary to work around an issue introduced by JUnit 4.13, where a test class which doesn't have
 * `@Parameters` method results in an error.
 * When you see an error message like `java.lang.Exception: No public static parameters method on class ...`, implement this
 * interface to fix it.
 */
public class JUnit4_13Workaround {
  @Parameters
  public static List<Object> dummyParametersMethod() {
    return emptyList();
  }
}
