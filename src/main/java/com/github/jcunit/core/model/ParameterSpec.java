package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Parameter;

import java.util.function.Function;

/**
 * // @formatter:off
 * @param <G> Generation-time parameter type
 * @param <E> Execution-time parameter type
 * // @formatter:on
 */
public interface ParameterSpec<G, E> {
  static <E> ParameterFactory<Parameter<String>, String, E> reference(String name, ParameterSpaceSpec parameterSpaceSpec) {
    return new ParameterFactoryImpl<>(
        name,
        Function.identity(),
        null,
        false,
        parameterSpaceSpec
    );
  }
}

