package com.github.jcunit.pipeline.stages.generators;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.pipeline.PipelineConfig;
import com.github.jcunit.pipeline.stages.Generator;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Cartesian extends Generator.Base {
  public Cartesian(FactorSpace factorSpace, PipelineConfig pipelineConfig) {
    super(factorSpace, pipelineConfig);
  }

  @Override
  protected List<Tuple> generateCore() {
    return factorSpace.stream()
                      .filter((Tuple tuple) -> factorSpace.getConstraints()
                                                          .stream()
                                                          .allMatch((Constraint constraint) -> constraint.test(tuple)))
                      .collect(toList());
  }
}
