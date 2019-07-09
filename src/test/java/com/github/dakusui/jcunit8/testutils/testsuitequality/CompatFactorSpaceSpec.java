package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit8.extras.abstracter.FactorSpaceSpec;

public class CompatFactorSpaceSpec extends FactorSpaceSpec {
  private final String                                       prefix;

  public CompatFactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String prefix() {
    return prefix;
  }
}
