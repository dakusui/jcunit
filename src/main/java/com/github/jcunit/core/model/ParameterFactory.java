package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Parameter;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 *
 * // @formatter:on
 *
 * @param <G> Generation-time parameter type.
 * @param <E> Execution-time parameter type.
 */
public interface ParameterFactory<G extends Function<Tuple, E>, E> {
  @FunctionalInterface
  interface ValueResolver<G> extends Function<Tuple, G> {
  
  }
  
  Parameter<G> toParameter(String name, List<String> arguments);
  
  E executionTimeValueFor(Tuple testData);
  
  abstract class Base<G extends ValueResolver<E>, E> implements ParameterFactory<G, E> {
    private final Function<Tuple, E> func;
    
    protected Base(Function<Tuple, E> func) {
      this.func = requireNonNull(func);
    }
    
    public E executionTimeValueFor(Tuple testData) {
      return this.func.apply(testData);
    }
  }
  
  class ParsingAsInt extends ParameterFactory.Base<ValueResolver<Integer>, Integer> {
    protected ParsingAsInt(Function<Tuple, Integer> func) {
      super(func);
    }
    
    @Override
    public Parameter<ValueResolver<Integer>> toParameter(String name, List<String> arguments) {
      // @formatter:off
      return new Parameter.Simple.Impl<>(name, arguments.stream()
                                                        .map(v -> (ValueResolver<Integer>)(Tuple tuple)-> Integer.parseInt(v))
                                                        .collect(toList()));
      // @formatter:on
    }
  }
  
  class ParsingAsDecimal extends ParameterFactory.Base<ValueResolver<BigDecimal>, BigDecimal> {
    protected ParsingAsDecimal(Function<Tuple, BigDecimal> func) {
      super(func);
    }
    
    @Override
    public Parameter<ValueResolver<BigDecimal>> toParameter(String name, List<String> arguments) {
      // @formatter:off
      return new Parameter.Simple.Impl<>(name, arguments.stream()
                                                        .map(v -> (ValueResolver<BigDecimal>)(Tuple tuple)-> new BigDecimal(v))
                                                        .collect(toList()));
      // @formatter:on
    }
  }
  
  class StringLevels extends ParameterFactory.Base<ValueResolver<String>, String> {
    
    
    protected StringLevels(Function<Tuple, String> func) {
      super(func);
    }
    
    @Override
    public Parameter<ValueResolver<String>> toParameter(String name, List<String> arguments) {
      throw new RuntimeException("TODO");
    }
  }
  
  class ValueResolvingMethodNames extends ParameterFactory.Base<ValueResolver<Object>, Object> {
    
    
    protected ValueResolvingMethodNames(Function<Tuple, Object> func) {
      super(func);
    }
    
    @Override
    public Parameter<ValueResolver<Object>> toParameter(String name, List<String> arguments) {
      throw new RuntimeException("TODO");
    }
  }
}
