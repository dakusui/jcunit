package com.github.jcunit.pipeline.stages.generators;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.pipeline.stages.Generator;

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
