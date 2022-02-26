package com.github.dakusui.jcunitx.tests.components.utils;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.core.StreamableRowCartesianator;
import com.github.dakusui.jcunitx.factorspace.Factor;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.functions.Predicates.equalTo;
import static com.github.dakusui.pcond.functions.Predicates.isEmpty;
import static java.util.Arrays.asList;

public class StreamableRowCartesianatorTest {
  @Test
  public void test() {
    new StreamableRowCartesianator(
        asList(
            Factor.create("F", new Object[] { "f1", "f2" }),
            Factor.create("G", new Object[] { "g1", "g2", "g3" }),
            Factor.create("H", new Object[] { "h1", "h2" }),
            Factor.create("I", new Object[] { "i1", "i2" })
        )
    ).cursor(
        AArray.builder()
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
    List<AArray> diffs = new LinkedList<>();
    AtomicInteger i = new AtomicInteger(0);
    StreamableRowCartesianator cartesianator = new StreamableRowCartesianator(factors);
    cartesianator
        .stream()
        .filter(each -> !Objects.equals(each, cartesianator.get(i.getAndIncrement())))
        .forEach(diffs::add);
    assertThat(diffs, isEmpty());
    assertThat(i.get(), equalTo(48));
  }
}
