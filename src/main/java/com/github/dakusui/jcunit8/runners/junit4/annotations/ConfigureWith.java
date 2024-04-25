package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Inherited
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
   * @return A class that defines parameter space or {@code Object.class}.
   * @see com.github.dakusui.jcunit8.factorspace.ParameterSpace
   * @see ParameterSource
   * @see Condition
   */
  Class<?> parameterSpace() default Object.class;
}
