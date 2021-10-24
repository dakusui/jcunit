package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(QuadraticEquationExampleWithSeeds.Config.class)
public class QuadraticEquationExampleWithSeeds extends QuadraticEquationExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(true)
          .addSeed(
              // Positive test
              KeyValuePairs.builder().put("a", 1).put("b", -2).put("c", 1).buildRow())
          .addSeed(
              // Negative test
              KeyValuePairs.builder().put("a", 1).put("b", 1).put("c", 1).buildRow())
          .build();
    }
  }
}
