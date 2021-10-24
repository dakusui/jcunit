package com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(MissingParameter.Config.class)
public class MissingParameter extends SeedBase {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
          KeyValuePairs.builder()
              .put("parameter1", "hello")
              .buildRow()
      ).build();
    }
  }
}
