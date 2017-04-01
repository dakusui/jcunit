package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;
import java.util.stream.Collectors;

public class Cartesian extends Generator.Base {
  public Cartesian(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(seeds, factorSpace, requirement);
  }

  @Override
  protected List<Tuple> generateCore() {
    return new StreamableTupleCartesianator(factorSpace.getFactors()).stream().collect(Collectors.toList());
  }
}
