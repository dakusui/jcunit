package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.Aarray;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@ConfigureWith(QuadraticEquationExampleWithSeeds.Config.class)
public class QuadraticEquationExampleWithSeeds extends QuadraticEquationExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          true
      ).addSeed(
          // Positive test
          Aarray.builder().put("a", 1).put("b", -2).put("c", 1).build()
      ).addSeed(
          // Negative test
          Aarray.builder().put("a", 1).put("b", 1).put("c", 1).build()
      ).build();
    }
  }
}
