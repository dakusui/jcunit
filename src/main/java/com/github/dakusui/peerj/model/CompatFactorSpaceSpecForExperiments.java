package com.github.dakusui.peerj.model;

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
