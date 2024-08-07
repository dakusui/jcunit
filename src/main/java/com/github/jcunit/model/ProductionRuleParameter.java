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
public class ProductionRuleParameter<T> extends Parameter.Base<T> implements Parameter<T> {
  /**
   * You can specify known values of this object through `knownValues` parameter.
   * Thia can be used to model "seed" values or "negative" values.
   *
   * @param name        A name of a factor created by this `Parameter`.
   * @param knownValues Known values of this parameter.
   */
  protected ProductionRuleParameter(String name, List<T> knownValues) {
    super(name, knownValues);
  }

  @Override
  protected List<Factor> decompose() {
    return Collections.emptyList();
  }

  @Override
  protected List<Constraint> generateConstraints() {
    return Collections.emptyList();
  }

  @Override
  public T composeValue(Tuple tuple) {
    return null;
  }

  @Override
  public Optional<Tuple> decomposeValue(T value) {
    return Optional.empty();
  }
}
