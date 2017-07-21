package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Cartesian extends Generator.Base {
  public Cartesian(FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
  }

  @Override
  protected List<Tuple> generateCore() {
    return Stream.concat(
        requirement.seeds().stream(),
        factorSpace.stream().filter(
            (Tuple tuple) -> !requirement.seeds().contains(tuple)
        ).filter(
            (Tuple tuple) -> factorSpace.getConstraints().stream()
                .allMatch(
                    (Constraint constraint) -> constraint.test(tuple)
                )
        )
    ).collect(
        Collectors.toList()
    );
  }
}
