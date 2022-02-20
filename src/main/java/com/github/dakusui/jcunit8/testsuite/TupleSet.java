package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Aarray;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TupleSet extends Set<Aarray> {
  TupleSet cartesianProduct(TupleSet tupleSet);

  class Impl extends LinkedHashSet<Aarray> implements TupleSet {
    public Impl(Collection<Aarray> tuples) {
      this.addAll(tuples);
    }

    @Override
    public TupleSet cartesianProduct(TupleSet rhs) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (Aarray eachFromLhs : this) {
        for (Aarray eachFromRhs : rhs) {
          builder.add(new Aarray.Builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
        }
      }
      return builder.build();
    }
  }

  class Builder {
    private final Set<Aarray> work;

    public Builder() {
      this.work = new LinkedHashSet<>();
    }

    public Builder addAll(Collection<Aarray> tuples) {
      this.work.addAll(tuples);
      return this;
    }

    public Builder add(Aarray tuple) {
      this.work.add(tuple);
      return this;
    }

    public TupleSet build() {
      return new Impl(this.work);
    }

  }
}
