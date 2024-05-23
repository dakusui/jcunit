package com.github.jcunit.annotations;


import com.github.jcunit.core.model.ParameterResolver;
import com.github.jcunit.factorspace.Parameter;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation to define a parameter in a parameter space, which is defined by {@link DefineParameterSpace}.
 */
@Retention(RUNTIME)
public @interface DefineParameter {
  String name();

  String[] with() default {};


  /**
   * @return A class for converting arguments specified by {@link DefineParameter#with()} to a {@link Parameter} object
   */
  Class<? extends ParameterResolver> as() default ParameterResolver.class;

}
