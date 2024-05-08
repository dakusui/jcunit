package com.github.jcunit.annotations;


import com.github.jcunit.core.model.ExecutionTimeValueResolver;
import com.github.jcunit.core.model.GenerationTimeParameterFactory;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.testsuite.TestCase;
import com.github.jcunit.testsuite.TestSuite;

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
  Class<? extends GenerationTimeParameterFactory> as() default GenerationTimeParameterFactory.class;

  /**
   * Specifies a class whose instance converts a {@link Tuple} returned by {@link TestCase#getTestData()} into an execution-time
   * value of this parameter.
   *
   * @return A class for resolving a value of this parameter from a {@link Tuple} in a generated {@link TestSuite}.
   */
  Class<? extends ExecutionTimeValueResolver.Factory> using() default ExecutionTimeValueResolver.Factory.class;
}
