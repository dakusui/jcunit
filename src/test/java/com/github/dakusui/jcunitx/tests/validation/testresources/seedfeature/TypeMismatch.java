package com.github.dakusui.jcunitx.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(TypeMismatch.Config.class)
public class TypeMismatch extends SeedBase {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
          AArray.builder(
          ).put(
              "parameter1", "hello"
          ).put(
              "parameter2", new Object()
          ).build()
      ).build();
    }
  }
}
