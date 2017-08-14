package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.factorspace.Factor;
import org.junit.Test;

import java.util.List;

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
        Tuple.builder()
            .put("F", "f1")
            .put("G", "g2")
            .put("H", "h1")
            .put("I", "i2")
            .build()
    ).stream(
    ).forEach(
        System.out::println
    );
  }

  @Test
  public void reproduce() {
    List<Factor> factors = asList(
        Factor.create("I", new Object[] { "i1", "i2", "i3" }),
        Factor.create("F", new Object[] { "f1", "f2" }),
        Factor.create("G", new Object[] { "g1", "g2" }),
        Factor.create("H", new Object[] { "h1", "h2" }),
        Factor.create("J", new Object[] { "j1", "j2" })
    );

    StreamableTupleCartesianator cartesianator = new StreamableTupleCartesianator(
        factors
    );
    cartesianator.stream(
    ).forEach(
        tuple -> System.out.printf("%s%n%s%n--%n", tuple, cartesianator.get(cartesianator.indexOf(tuple)))
    );
  }

}
