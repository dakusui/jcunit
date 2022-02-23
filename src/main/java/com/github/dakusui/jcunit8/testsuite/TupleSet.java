package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.AArray;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TupleSet extends Set<AArray> {
  TupleSet cartesianProduct(TupleSet tupleSet);

  class Impl extends LinkedHashSet<AArray> implements TupleSet {
    public Impl(Collection<AArray> tuples) {
      this.addAll(tuples);
    }

    @Override
    public TupleSet cartesianProduct(TupleSet rhs) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (AArray eachFromLhs : this) {
        for (AArray eachFromRhs : rhs) {
          builder.add(new AArray.Builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
        }
      }
      return builder.build();
    }
  }

  class Builder {
    private final Set<AArray> work;

    public Builder() {
      this.work = new LinkedHashSet<>();
    }

    public Builder addAll(Collection<AArray> tuples) {
      this.work.addAll(tuples);
      return this;
    }

    public Builder add(AArray tuple) {
      this.work.add(tuple);
      return this;
    }

    public TupleSet build() {
      return new Impl(this.work);
    }

  }
}
