package com.github.jcunit.pipeline;

import com.github.jcunit.core.tuples.Tuple;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public interface TupleSet extends Set<Tuple> {
  TupleSet cartesianProduct(TupleSet tupleSet);

  class Impl extends LinkedHashSet<Tuple> implements TupleSet {
    public Impl(Collection<Tuple> tuples) {
      this.addAll(tuples);
    }

    @Override
    public TupleSet cartesianProduct(TupleSet rhs) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (Tuple eachFromLhs : this) {
        for (Tuple eachFromRhs : rhs) {
          builder.add(new Tuple.Builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
        }
      }
      return builder.build();
    }
  }

  class Builder {
    private final Set<Tuple> work;

    public Builder() {
      this.work = new LinkedHashSet<>();
    }

    public Builder addAll(Collection<Tuple> tuples) {
      this.work.addAll(tuples);
      return this;
    }

    public Builder add(Tuple tuple) {
      this.work.add(tuple);
      return this;
    }

    public TupleSet build() {
      return new Impl(this.work);
    }

  }
}
