package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ConfigureWith {
  Class<? extends ConfigFactory> value();

  interface ConfigFactory {
    Config<Tuple> create();

    abstract class Base implements ConfigFactory {
      abstract protected Requirement requirement();

      abstract protected Generator.Factory generatorFactory();

      @Override
      public Config<Tuple> create() {
        return Config.Builder.forTuple(requirement()).withGeneratorFactory(generatorFactory()).build();
      }
    }

    class Default extends Base {
      @SuppressWarnings("WeakerAccess")
      protected Requirement requirement() {
        return new Requirement.Builder()
            .withStrength(2)
            .withNegativeTestGeneration(false)
            .build();
      }

      @Override
      protected Generator.Factory generatorFactory() {
        return IpoG::new;
      }
    }
  }
}
