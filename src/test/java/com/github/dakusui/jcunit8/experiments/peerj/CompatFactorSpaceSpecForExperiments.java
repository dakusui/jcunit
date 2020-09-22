package com.github.dakusui.jcunit8.experiments.peerj;

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
