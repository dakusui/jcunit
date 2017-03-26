package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;

public class Passthrough extends Generator.Base {
  public Passthrough(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(seeds, factorSpace, requirement);
  }

  @Override
  public List<Tuple> generate() {
    return this.seeds;
  }
}
