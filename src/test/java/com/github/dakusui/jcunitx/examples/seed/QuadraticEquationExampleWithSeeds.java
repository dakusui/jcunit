package com.github.dakusui.jcunitx.examples.seed;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;

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
          AArray.builder().put("a", 1).put("b", -2).put("c", 1).build()
      ).addSeed(
          // Negative test
          AArray.builder().put("a", 1).put("b", 1).put("c", 1).build()
      ).build();
    }
  }
}
