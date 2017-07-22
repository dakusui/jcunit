package com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(UnknownParameter.Config.class)
public class UnknownParameter extends SeedBase {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
          Tuple.builder(
          ).put(
              "parameter1", "hello"
          ).put(
              "parameter2", "hello"
          ).put(
              "unknownParameter", "hello"
          ).build()
      ).build();
    }
  }
}
