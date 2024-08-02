package com.github.jcunit.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.Parameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class SequenceParameter<T> extends Parameter.Base<List<ValueResolver<T>>> implements Parameter<List<ValueResolver<T>>> {

  protected SequenceParameter(String name,

                              List<List<ValueResolver<T>>> knownValues) {
    super(name, knownValues);
  }

  @Override
  public List<Factor> decompose() {
    return Collections.emptyList();
  }

  @Override
  protected List<Constraint> generateConstraints() {
    return Collections.emptyList();
  }

  @Override
  public List<ValueResolver<T>> composeValue(Tuple tuple) {
    return null;
  }

  @Override
  public Optional<Tuple> decomposeValue(List<ValueResolver<T>> value) {
    return Optional.empty();
  }
}
