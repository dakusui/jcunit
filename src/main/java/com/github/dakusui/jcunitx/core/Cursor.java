package com.github.dakusui.jcunitx.core;

import com.github.dakusui.combinatoradix.Enumerator;

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

  class ForRow extends Base<AArray> {
    private final StreamableRowCartesianator enumerator;

    ForRow(long startFrom, StreamableRowCartesianator enumerator) {
      super(startFrom);
      this.enumerator = enumerator;
    }

    @Override
    public Iterator<AArray> iterator() {
      // TODO avoid using 'int'
      // return this.enumerator.asList().subList((int) startFrom, (int) this.enumerator.size()).iterator();
      return new Iterator<AArray>() {
        long i = startFrom;

        @Override
        public boolean hasNext() {
          return i < enumerator.size();
        }

        @Override
        public AArray next() {
          try {
            return enumerator.get(i);
          } finally {
            i++;
          }
        }
      };    }
  }
}
