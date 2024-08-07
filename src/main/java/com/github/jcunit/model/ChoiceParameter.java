package com.github.jcunit.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.Parameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ChoiceParameter<T> extends Parameter.Base<T> {
  private final List<T> values;

  /**
   * You can specify known values of this object through `knownValues` parameter.
   * Thia can be used to model "seed" values or "negative" values.
   *
   * @param name        A name of a factor created by this `Parameter`.
   * @param knownValues Known values of this parameter.
   */
  protected ChoiceParameter(String name, List<T> knownValues, List<T> values) {
    super(name, knownValues);
    this.values = requireNonNull(values);
  }

  @Override
  protected List<Factor> decompose() {
    return Collections.singletonList(Factor.create(this.getName(), values.toArray()));
  }

  @Override
  protected List<Constraint> generateConstraints() {
    return Collections.emptyList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T composeValue(Tuple tuple) {
    return (T) tuple.get(this.getName());
  }

  @Override
  public Optional<Tuple> decomposeValue(T value) {
    if (!values.contains(value)) {
      return Optional.empty();
    }
    return Optional.of(Tuple.builder()
                            .put(getName(), value)
                            .build());
  }
}
