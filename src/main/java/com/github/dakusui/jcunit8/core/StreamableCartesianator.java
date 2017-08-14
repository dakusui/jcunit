package com.github.dakusui.jcunit8.core;

import com.github.dakusui.combinatoradix.Cartesianator;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;

public class StreamableCartesianator<E> extends Cartesianator<E> {

  public StreamableCartesianator(List<? extends List<? extends E>> sets) {
    super(sets);
  }

  @SuppressWarnings("unchecked")
  public StreamableCartesianator(List<? extends E>... sets) {
    this(asList(sets));
  }

  public Cursor<List<E>> cursor(List<E> at) {
    return new Cursor.Impl<>(indexOf(at), this);
  }

  public Stream<List<E>> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }
}
