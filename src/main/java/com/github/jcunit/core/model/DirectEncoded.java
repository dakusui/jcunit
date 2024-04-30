package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.Parameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface DirectEncoded<T> extends Parameter<T> {
  class Impl<T> extends Base<T> implements DirectEncoded<T> {
    public Impl(String name, List<T> knownValues) {
      super(name, knownValues);
    }
    
    @Override
    protected List<Factor> decompose() {
      return singletonList(Factor.create(this.getName(), encodeIntLevels(getKnownValues())));
    }
    
    private static <T> Object[] encodeIntLevels(List<T> knownValues) {
      return IntStream.range(0, knownValues.size()).boxed().toArray();
    }
    
    @Override
    protected List<Constraint> generateConstraints() {
      return emptyList();
    }
    
    @Override
    public T composeValue(Tuple tuple) {
      return getKnownValues().get((int) tuple.get(name));
    }
    
    @Override
    public Optional<Tuple> decomposeValue(T value) {
      return Optional.of(Tuple.builder().put(name, getKnownValues().indexOf(value)).build());
    }
  }
}
