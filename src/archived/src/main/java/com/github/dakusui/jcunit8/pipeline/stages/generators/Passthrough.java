package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;

public class Passthrough extends Generator.Base {
  private final List<Tuple> testCases;

  public Passthrough(List<Tuple> testCases, FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
    this.testCases = testCases;
  }

  @Override
  public List<Tuple> generateCore() {
    return testCases;
  }
}
