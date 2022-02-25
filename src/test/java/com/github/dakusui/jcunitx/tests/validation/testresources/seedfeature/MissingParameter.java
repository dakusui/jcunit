package com.github.dakusui.jcunitx.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@ConfigureWith(MissingParameter.Config.class)
public class MissingParameter extends SeedBase {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
          AArray.builder(
          ).put(
              "parameter1", "hello"
          ).build()
      ).build();
    }
  }
}
