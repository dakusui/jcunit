package com.github.dakusui.jcunit8.core;

import com.github.dakusui.combinatoradix.Combinator;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamableCombinator<T> extends Combinator<T> {
  public StreamableCombinator(List<? extends T> items, int k) {
    super(items, k);
  }

  public Stream<List<T>> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }
}
