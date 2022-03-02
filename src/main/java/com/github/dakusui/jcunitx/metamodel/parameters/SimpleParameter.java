package com.github.dakusui.jcunitx.metamodel.parameters;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.metamodel.Parameter;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * A simple parameter, which holds several possible values defined by "equivalence partitioning".
 * This directly corresponds to an entity called "factor" in the combinatorial interaction testing terminology.
 *
 * @param <T> The type of values.
 */
public interface SimpleParameter<T> extends Parameter<T> {
  class Impl<T> extends Base<T> implements SimpleParameter<T> {
    final Factor factor;

    public Impl(String name, List<T> allLevels) {
      super(name, allLevels);
      this.factor = Factor.create(name, allLevels.toArray());
    }

    @Override
    protected List<Factor> decompose() {
      return singletonList(factor);
    }

    @Override
    protected List<Constraint> generateConstraints() {
      return emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T composeValue(AArray tuple) {
      return (T) tuple.get(getName());
    }

    @Override
    public Optional<AArray> decomposeValue(T value) {
      return Optional.of(AArray.builder().put(name, value).build());
    }

    @Override
    public String toString() {
      return String.format("Simple:%s:%s", factor.getName(), factor.getLevels());
    }
  }

  class Descriptor<T> extends Parameter.Descriptor.Base<T> {

    private Descriptor() {
    }

    public static <U> Descriptor<U> of(List<U> actualValues) {
      return new Descriptor<U>().addActualValues(actualValues);
    }

    @Override
    public Parameter<T> create(String name) {
      return new Impl<>(name, this.knownValues);
    }
  }
}
