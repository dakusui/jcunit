package com.github.jcunit.pipeline.stages.generators;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.pipeline.stages.Generator;

import java.util.List;
import java.util.stream.Collectors;

public class Cartesian extends Generator.Base {
  public Cartesian(FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
  }

  @Override
  protected List<Tuple> generateCore() {
    return factorSpace.stream(
    ).filter(
        (Tuple tuple) -> factorSpace.getConstraints().stream()
            .allMatch(
                (Constraint constraint) -> constraint.test(tuple)
            )
    ).collect(
        Collectors.toList()
    );
  }
}
