package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ParameterFactory<P extends Parameter<T>, T, E> {
  List<Constraint> createConstraint();

  ExecutionTimeValueResolver<T, E> valueResolver();

  String name();
}
