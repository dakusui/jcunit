package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;

public class CompatFactorSpaceSpecForExperiments extends FactorSpaceSpecForExperiments {
  private final String                                       prefix;

  public CompatFactorSpaceSpecForExperiments(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String prefix() {
    return prefix;
  }
}
