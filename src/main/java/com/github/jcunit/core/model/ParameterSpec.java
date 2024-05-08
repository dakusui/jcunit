package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Parameter;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ParameterSpec<P extends Parameter<T>, T, E> {
  GenerationTimeParameterFactory<P, T> parameterFactory();

  ExecutionTimeValueResolver<T, E> valueResolver();

  String name();
}
