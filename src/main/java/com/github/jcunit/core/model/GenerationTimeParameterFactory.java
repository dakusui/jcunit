package com.github.jcunit.core.model;

import com.github.jcunit.annotations.DefineParameter;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface GenerationTimeParameterFactory<P extends Parameter<T>, T> {
  /**
   * @param args Values specified by {@link DefineParameter#with()}.
   * @return A parameter.
   */
  P createParameter(String... args);

  /**
   * @param args Values specified by {@link DefineParameter#with()}.
   * @return Constraints required by the parameter created by {@link GenerationTimeParameterFactory#createParameter(String...)} method.
   */
  List<Constraint> createConstraints(String... args);
}
