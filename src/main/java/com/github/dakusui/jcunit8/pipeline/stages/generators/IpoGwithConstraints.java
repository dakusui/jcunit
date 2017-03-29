package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;

import java.util.List;

public class IpoGwithConstraints extends IpoG {

  public IpoGwithConstraints(List<Tuple> seeds, Requirement requirement, FactorSpace factorSpace) {
    super(seeds, factorSpace, requirement);
  }

  @Override
  public List<Tuple> generate() {
    return null;
  }
}
