package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
class Simple<T> implements ParameterSpec<Parameter<T>, T, T> {
  private final Function<String, T> function;
  private final String name;

  public Simple(String name, Function<String, T> function) {
    this.name = name;
    this.function = function;
  }

  @Override
  public GenerationTimeParameterFactory<Parameter<T>, T> parameterFactory() {
    return new GenerationTimeParameterFactory<Parameter<T>, T>() {
      @Override
      public Parameter<T> createParameter(String... args) {
        return Parameter.Simple.Factory.of(Arrays.stream(args).map(function).collect(toList())).create(name());
      }

      @Override
      public List<Constraint> createConstraints() {
        return emptyList();
      }
    };
  }

  @Override
  public ExecutionTimeValueResolver<T, T> valueResolver() {
    return (generationTimeValue, testData) -> generationTimeValue;
  }

  @Override
  public String name() {
    return this.name;
  }
}
