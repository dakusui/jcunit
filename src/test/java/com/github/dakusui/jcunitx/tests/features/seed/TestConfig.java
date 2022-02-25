package com.github.dakusui.jcunitx.tests.features.seed;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.ConfigFactory;

public abstract class TestConfig extends ConfigFactory.Base {
  public static class SeedNone$NegativeTestEnabled extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          true
      ).build();
    }
  }

  public static class SeedSa$NegativeTestDisabled extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.addSeed(
          AArray.builder().put("a", 0).put("b", 0).put("c", 0).build() // Sa
      ).withNegativeTestGeneration(
          false
      ).build();
    }
  }

  public static class SeedSa$NegativeTestEnabled extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.addSeed(
          AArray.builder().put("a", 0).put("b", 0).put("c", 0).build() // Sa
      ).withNegativeTestGeneration(
          true
      ).build();
    }
  }

  public static class SeedSaAndSb$NegativeEnabled extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.addSeed(
          AArray.builder().put("a", 0).put("b", 0).put("c", 0).build() // Sa
      ).addSeed(
          AArray.builder().put("a", 1).put("b", 1).put("c", 1).build() // Sb
      ).withNegativeTestGeneration(
          true
      ).build();
    }
  }

  public static class SeedSb$NegativeEnabled extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.addSeed(
          AArray.builder().put("a", 1).put("b", 1).put("c", 1).build() // Sb
      ).withNegativeTestGeneration(
          true
      ).build();
    }
  }
}
