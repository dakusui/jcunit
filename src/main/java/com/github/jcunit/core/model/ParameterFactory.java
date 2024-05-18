package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ParameterFactory<P extends Parameter<T>, T, E> {
  P createParameter(String[] args);
  List<Constraint> createConstraint();

  ExecutionTimeValueResolver<T, E> valueResolver();

  String name();

  interface ValueResolver<V> {
    V resolve(Tuple testData);
  }
}
