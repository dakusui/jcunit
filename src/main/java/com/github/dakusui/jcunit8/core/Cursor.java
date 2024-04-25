package com.github.dakusui.jcunit8.core;

import com.github.dakusui.combinatoradix.Enumerator;
import com.github.dakusui.jcunit8.core.tuples.Tuple;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Cursor<E> extends Iterable<E> {
  Stream<E> stream();

  abstract class Base<E> implements Cursor<E> {
    final long startFrom;

    Base(long startFrom) {

      this.startFrom = startFrom;
    }

    public Stream<E> stream() {
      return StreamSupport.stream(this.spliterator(), false);
    }
  }

  class Impl<T> extends Base<List<T>> {
    final Enumerator<T> enumerator;

    public Impl(long startFrom, Enumerator<T> enumerator) {
      super(startFrom);
      this.enumerator = enumerator;
    }

    @Override
    public java.util.Iterator<List<T>> iterator() {
      return new Enumerator.Iterator<>(startFrom, enumerator);
    }
  }

  class ForTuple extends Base<Tuple> {
    private final StreamableTupleCartesianator enumerator;

    ForTuple(long startFrom, StreamableTupleCartesianator enumerator) {
      super(startFrom);
      this.enumerator = enumerator;
    }

    @Override
    public Iterator<Tuple> iterator() {
      // TODO avoid using 'int'
      // return this.enumerator.asList().subList((int) startFrom, (int) this.enumerator.size()).iterator();
      return new Iterator<Tuple>() {
        long i = startFrom;

        @Override
        public boolean hasNext() {
          return i < enumerator.size();
        }

        @Override
        public Tuple next() {
          try {
            return enumerator.get(i);
          } finally {
            i++;
          }
        }
      };    }
  }
}
