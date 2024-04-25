package com.github.jcunit.pipeline.stages;

import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Requirement;

public interface ConfigFactory {
  Config create();

  abstract class Base implements ConfigFactory {
    protected Requirement requirement() {
      return defineRequirement(defaultValues());
    }

    @Override
    public Config create() {
      return Config.Builder.forTuple(requirement()).withGeneratorFactory(generatorFactory()).build();
    }

    abstract protected Requirement defineRequirement(Requirement.Builder defaultValues);

    @SuppressWarnings("WeakerAccess")
    // To allow users to override, keep this method 'protected'.
    protected Generator.Factory generatorFactory() {
      return new Generator.Factory.Standard();
    }

    Requirement.Builder defaultValues() {
      return new Requirement.Builder()
          .withStrength(2)
          .withNegativeTestGeneration(false);
    }
  }

  class Default extends Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.build();
    }
  }
}
