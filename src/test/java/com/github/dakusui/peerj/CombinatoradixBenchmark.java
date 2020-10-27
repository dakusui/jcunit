package com.github.dakusui.peerj;

import com.github.dakusui.combinatoradix.Combinator;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CombinatoradixBenchmark {
  @Test
  public void test() {
    AtomicInteger v = new AtomicInteger();
    new Combinator<>(
        IntStream.range(0, 163).mapToObj((int i) -> "p" + i).collect(toList()),
        4).forEach(n -> v.getAndIncrement());
    System.out.println(v);
  }
}
