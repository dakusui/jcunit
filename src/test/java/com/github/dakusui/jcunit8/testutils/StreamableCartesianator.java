package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.combinatoradix.Cartesianator;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamableCartesianator<E> extends Cartesianator<E> {

  public StreamableCartesianator(List<? extends List<? extends E>> sets) {
    super(sets);
  }

  public Stream<List<E>> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }
}
