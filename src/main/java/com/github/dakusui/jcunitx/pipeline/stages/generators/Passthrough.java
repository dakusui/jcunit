package com.github.dakusui.jcunitx.pipeline.stages.generators;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.Generator;

import java.util.List;

public class Passthrough extends Generator.Base {
  private final List<AArray> testCases;

  public Passthrough(List<AArray> testCases, FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
    this.testCases = testCases;
  }

  @Override
  public List<AArray> generateCore() {
    return testCases;
  }
}
