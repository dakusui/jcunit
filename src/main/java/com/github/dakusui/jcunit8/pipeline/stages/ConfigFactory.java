package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;

public interface ConfigFactory {
  Config create();

  abstract class Base implements ConfigFactory {
    protected Requirement requirement() {
      return defineRequirement(defaultValues()).build();
    }

    @Override
    public Config create() {
      return Config.Builder.forTuple(requirement()).withGeneratorFactory(generatorFactory()).build();
    }

    abstract protected Requirement.Builder defineRequirement(Requirement.Builder builder);

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
    protected Requirement.Builder defineRequirement(Requirement.Builder builder) {
      return builder;
    }
  }
}
