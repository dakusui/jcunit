package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ConfigureWith {
  Class<? extends ConfigFactory> value();

  interface ConfigFactory {
    Config<Tuple> create();

    class Impl implements ConfigFactory {
      @SuppressWarnings("WeakerAccess")
      protected Requirement requirement() {
        return new Requirement.Builder()
            .withStrength(2)
            .withNegativeTestGeneration(false)
            .build();
      }

      @Override
      public Config<Tuple> create() {
        return new Config.Builder<Tuple>(requirement()).build();
      }
    }
  }
}
