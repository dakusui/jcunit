package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Aarray;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;

public class Passthrough extends Generator.Base {
  private final List<Aarray> testCases;

  public Passthrough(List<Aarray> testCases, FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
    this.testCases = testCases;
  }

  @Override
  public List<Aarray> generateCore() {
    return testCases;
  }
}
