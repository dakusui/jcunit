package com.github.jcunit.annotations;


import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.testsuite.TestCase;
import com.github.jcunit.testsuite.TestSuite;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation to define a parameter in a parameter space, which is defined by {@link DefineParameterSpace}.
 */
@Retention(RUNTIME)
public @interface DefineParameter {
  String name();
  String[] with() default {};
  
  /**
   *
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
  
  interface ExecutionTimeValueResolver<E> {
    default E resolve(TestCase testCase) {
      return resolve(testCase.getTestData());
    }
    
    E resolve(Tuple testData);
    
    interface Factory<E> {
      ExecutionTimeValueResolver<E> create(String name);
    }
  }
  
  interface GenerationTimeParameterFactory {
    /**
     *
     * @param args Values specified by {@link DefineParameter#with()}.
     * @return A parameter.
     */
    Parameter<Object> createParameter(String... args);
    /**
     *
     * @param args Values specified by {@link DefineParameter#with()}.
     * @return Constraints required by the parameter created by {@link GenerationTimeParameterFactory#createParameter(String...)} method.
     */
    List<Constraint> createConstraints(String... args);
  }
}
