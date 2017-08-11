package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.factorspace.Factor;
import org.junit.Test;

import static java.util.Arrays.asList;

public class StreamableTupleCartesianatorTest {
  @Test
  public void test() {
    new StreamableTupleCartesianator(
        asList(
            Factor.create("F", new Object[] { "f1", "f2" }),
            Factor.create("G", new Object[] { "g1", "g2", "g3" }),
            Factor.create("H", new Object[] { "h1", "h2" }),
            Factor.create("I", new Object[] { "i1", "i2" })
        )
    ).cursor(
        Tuple.builder().put("F", "f1").put("G", "g2").put("H", "h1").build()
    ).stream(
    ).forEach(
        System.out::println
    );
  }
}
