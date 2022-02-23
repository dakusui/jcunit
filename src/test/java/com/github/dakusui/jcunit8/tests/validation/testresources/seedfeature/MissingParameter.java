package com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

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
