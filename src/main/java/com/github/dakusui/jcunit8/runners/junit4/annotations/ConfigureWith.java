package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface ConfigureWith {
  ConfigureWith DEFAULT_INSTANCE = new ConfigureWith() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return ConfigureWith.class;
    }

    @Override
    public Class<? extends ConfigFactory> value() {
      return ConfigFactory.Default.class;
    }

    @Override
    public Class<?> parameterSpace() {
      return Object.class;
    }
  };

  Class<? extends ConfigFactory> value() default ConfigFactory.Default.class;

  /**
   * Specifies a class to define a parameter space, which has parameters, constraints
   * and non-constraint conditions. If this value is not used, (or {@code Object.class}
   * is specified, ) the same class to which {@code CondigureWith} annotation is
   * attached is used to create a parameter space Object.
   *
   * @see com.github.dakusui.jcunit8.factorspace.ParameterSpace
   * @see ParameterSource
   * @see com.github.dakusui.jcunit.runners.standard.annotations.Condition
   */
  Class<?> parameterSpace() default Object.class;

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
