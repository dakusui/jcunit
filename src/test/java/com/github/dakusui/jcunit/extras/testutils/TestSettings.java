package com.github.dakusui.jcunit.extras.testutils;

public class TestSettings {
  /**
   * Since checking if all the pairs are covered or not is very expensive
   * operation, it's set to false by default.
   *
   * @return true - enabled / false - disabled.
   */
  public static boolean isCoveringCheckEnabled() {
    return false;
  }
}
