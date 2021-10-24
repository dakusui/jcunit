package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TupleSet extends Set<KeyValuePairs> {
  TupleSet cartesianProduct(TupleSet tupleSet);

  class Impl extends LinkedHashSet<KeyValuePairs> implements TupleSet {
    public Impl(Collection<KeyValuePairs> tuples) {
      this.addAll(tuples);
    }

    @Override
    public TupleSet cartesianProduct(TupleSet rhs) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (KeyValuePairs eachFromLhs : this) {
        for (KeyValuePairs eachFromRhs : rhs) {
          builder.add(new KeyValuePairs.Builder().putAll(eachFromLhs).putAll(eachFromRhs).buildTuple());
        }
      }
      return builder.build();
    }
  }

  class Builder {
    private final Set<KeyValuePairs> work;

    public Builder() {
      this.work = new LinkedHashSet<>();
    }

    public Builder addAll(Collection<KeyValuePairs> tuples) {
      this.work.addAll(tuples);
      return this;
    }

    public Builder add(KeyValuePairs tuple) {
      this.work.add(tuple);
      return this;
    }

    public TupleSet build() {
      return new Impl(this.work);
    }

  }
}
