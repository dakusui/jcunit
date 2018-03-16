package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableCollection;

public interface TupleSet extends Iterable<Tuple> /* extends Set<Tuple>*/ {
  Stream<Tuple> stream();

  TupleSet cartesianProduct(TupleSet tupleSet);

  int size();

  boolean removeAll(Collection<Tuple> c);

  boolean remove(Tuple tuple);

  boolean isEmpty();

  boolean contains(Tuple tuple);

  List<Tuple> toList();

  Set<Tuple> toSet();

  Collection<Tuple> toUnmodifiableCollection();

  class Impl implements TupleSet {
    //private final Set<Tuple> content = new HashSet<>();
    private final Set<Tuple> content = new LinkedHashSet<>();

    public Impl(Collection<Tuple> tuples) {
      content.addAll(tuples);
    }

    @Override
    public TupleSet cartesianProduct(TupleSet rhs) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (Tuple eachFromLhs : this) {
        for (Tuple eachFromRhs : rhs) {
          builder.add(Tuple.builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
        }
      }
      return builder.build();
    }

    @Override
    public int size() {
      return content.size();
    }

    @Override
    public boolean removeAll(Collection<Tuple> c) {
      return content.removeAll(c);
    }

    @Override
    public boolean remove(Tuple tuple) {
      return content.remove(tuple);
    }

    @Override
    public boolean isEmpty() {
      return content.isEmpty();
    }

    @Override
    public boolean contains(Tuple tuple) {
      return content.isEmpty();
    }

    @Override
    public List<Tuple> toList() {
      return new LinkedList<>(this.content);
    }

    @Override
    public Set<Tuple> toSet() {
      return new LinkedHashSet<>(this.content);
    }

    @Override
    public Collection<Tuple> toUnmodifiableCollection() {
      return Collections.unmodifiableCollection(content);
    }

    @Override
    public Stream<Tuple> stream() {
      return content.stream();
    }

    @Override
    public Iterator<Tuple> iterator() {
      return content.iterator();
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

    public Builder remove(Tuple tuple) {
      this.work.remove(tuple);
      return this;
    }

    public TupleSet build() {
      return new Impl(this.work);
    }

    public Collection<Tuple> content() {
      return unmodifiableCollection(this.work);
    }
  }
}
