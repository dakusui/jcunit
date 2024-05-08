package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
class Regex<T> implements ParameterSpec<Parameter<List<String>>, List<String>, List<T>> {
  private final Function<String, T> function;
  private final String name;

  public Regex(String name, Function<String, T> function) {
    this.name = name;
    this.function = function;
  }

  @Override
  public GenerationTimeParameterFactory<Parameter<List<String>>, List<String>> parameterFactory() {
    return new GenerationTimeParameterFactory<Parameter<List<String>>, List<String>>() {
      @Override
      public Parameter<List<String>> createParameter(String... args) {
        return Parameter.Regex.Factory.of(args[0]).create(name);
      }

      @Override
      public List<Constraint> createConstraints() {
        return emptyList();
      }
    };
  }

  @Override
  public ExecutionTimeValueResolver<List<String>, List<T>> valueResolver() {
    return (generationTimeValue, testData) -> generationTimeValue.stream()
                                                                 .map(function)
                                                                 .collect(toList());
  }

  @Override
  public String name() {
    return name;
  }
}
