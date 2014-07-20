package com.github.dakusui.jcunit.framework.utils.tuples;

public class JCUnitAssertionError extends AssertionError {
  public JCUnitAssertionError() {
  }

  public JCUnitAssertionError(Object detailMessage) {
    super(detailMessage);
  }
}
