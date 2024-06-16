package com.github.jcunit.annotations;

import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.Pipeline;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Inherited
public @interface ConfigureWith {
  Class<? extends Pipeline.Standard> value() default Pipeline.Standard.class;

  String[] pipelineArguments() default {};

  /**
   * Specifies a class to define a parameter space, which has parameters, constraints
   * and non-constraint conditions. If this value is not used, (or {@code Object.class}
   * is specified, ) the same class to which {@code ConfigureWith} annotation is
   * attached is used to create a parameter space Object.
   *
   * @return A class that defines parameter space or {@code Object.class}.
   * @see ParameterSpace
   */
  Class<?> parameterSpace() default Object.class;
}
