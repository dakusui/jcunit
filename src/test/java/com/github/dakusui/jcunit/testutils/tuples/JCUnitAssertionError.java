package com.github.dakusui.jcunit.testutils.tuples;

public class JCUnitAssertionError extends AssertionError {
  public JCUnitAssertionError() {
  }

  public JCUnitAssertionError(Object detailMessage) {
    super(detailMessage);
  }
}
