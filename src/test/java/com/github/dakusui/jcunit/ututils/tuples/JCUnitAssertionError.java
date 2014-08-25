package com.github.dakusui.jcunit.ututils.tuples;

public class JCUnitAssertionError extends AssertionError {
  public JCUnitAssertionError() {
  }

  public JCUnitAssertionError(Object detailMessage) {
    super(detailMessage);
  }
}
