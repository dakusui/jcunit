package com.github.dakusui.jcunitx.pipeline.stages.generators;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.Generator;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Cartesian extends Generator.Base {
  public Cartesian(FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
  }

  @Override
  protected List<AArray> generateCore() {
    return factorSpace.streamAllPossibleRows()
        .filter((AArray row) -> satisfiesAllConstraints(row, factorSpace))
        .collect(toList());
  }

  private boolean satisfiesAllConstraints(AArray row, FactorSpace factorSpace) {
    return factorSpace.getConstraints()
        .stream()
        .allMatch((Constraint constraint) -> constraint.test(row));
  }
}
