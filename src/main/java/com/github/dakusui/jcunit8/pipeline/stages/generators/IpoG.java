package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;

public class IpoG extends Generator.Base {

  public IpoG( List<Tuple> seeds, Requirement requirement, FactorSpace factorSpace) {
    super(seeds, factorSpace, requirement);
  }

  @Override
  public List<Tuple> generate() {
    return null;
  }
}
