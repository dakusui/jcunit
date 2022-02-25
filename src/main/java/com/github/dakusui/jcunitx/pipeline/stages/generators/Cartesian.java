package com.github.dakusui.jcunitx.pipeline.stages.generators;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.Generator;

import java.util.List;
import java.util.stream.Collectors;

public class Cartesian extends Generator.Base {
  public Cartesian(FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
  }

  @Override
  protected List<AArray> generateCore() {
    return factorSpace.stream(
    ).filter(
        (AArray tuple) -> factorSpace.getConstraints().stream()
            .allMatch(
                (Constraint constraint) -> constraint.test(tuple)
            )
    ).collect(
        Collectors.toList()
    );
  }
}
