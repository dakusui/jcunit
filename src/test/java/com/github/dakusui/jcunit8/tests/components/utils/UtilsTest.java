package com.github.dakusui.jcunit8.tests.components.utils;

import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.experiments.JoinDataSet;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void givenString$whenPrint$thenNotBroken() {
    assertEquals("Hello", Utils.print("Hello"));
    assertEquals("Hello2", Utils.print("Hello2"));
  }

  @Test
  public void max1() {
    assertThat(
        Utils.max(Stream.of(1, 2, 3, 2, 3, 4), 5, v -> (long) v),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asInteger("get").eq(4).$()
        )
    );
  }

  @Test
  public void max2() {
    assertThat(
        Utils.max(Stream.of(1, 2, 3, 2, 3, 4), 4, v -> (long) v),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asInteger("get").eq(4).$()
        )
    );
  }

  @Test
  public void max3() {
    assertThat(
        Utils.max(Stream.of(1, 2, 3, 2, 3, 4), 2, v -> (long) v),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asInteger("get").eq(2).$()
        )
    );
  }


  @Test
  public void min1() {
    assertThat(
        Utils.min(Stream.of(1, 2, 3, 2, 3, 0, 4), 5, v -> (long) v),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asInteger("get").eq(1).$()
        )
    );
  }

  @Test
  public void min2() {
    assertThat(
        Utils.min(Stream.of(1, 2, 3, 2, 3, 0, 4), -1, v -> (long) v),
        allOf(
            asBoolean("isPresent").isTrue().$(),
            asInteger("get").eq(0).$()
        )
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cartesian() {
    assertThat(
        Utils.cartesian(
            Stream.of("hello", "world"),
            Stream.of("1", "2"),
            Stream.of("X", "Y", "Z")
        ).collect(toList()),
        allOf(
            asInteger("size").eq(12).$(),
            asObject("get", 0).equalTo(asList("hello", "1", "X")).$(),
            asObject("get", 1).equalTo(asList("hello", "1", "Y")).$()
        )
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cartesian2() {
    assertThat(
        Utils.cartesian(
            Stream.of("hello", "world"),
            Stream.of("1"),
            Stream.of("X", "Y", "Z")
        ).collect(toList()),
        allOf(
            asInteger("size").eq(6).$(),
            asObject("get", 0).equalTo(asList("hello", "1", "X")).$(),
            asObject("get", 1).equalTo(asList("hello", "1", "Y")).$()
        )
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cartesian3() {
    assertThat(
        Utils.cartesian(
            Stream.of("hello", "world"),
            Stream.of("X", "Y", "Z")
        ).collect(toList()),
        allOf(
            asInteger("size").eq(6).$(),
            asObject("get", 0).equalTo(asList("hello", "X")).$(),
            asObject("get", 1).equalTo(asList("hello", "Y")).$()
        )
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cartesian4() {
    assertThat(
        Utils.cartesian(
            Stream.of("hello", "world"),
            Stream.of("X", "Y", "Z"),
            Stream.empty()
        ).collect(toList()),
        allOf(
            asInteger("size").eq(0).$()
        )
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cartesian5() {
    assertThat(
        Utils.cartesian(
            Stream.of("hello", "world")
        ).collect(toList()),
        allOf(
            asInteger("size").eq(2).$(),
            asObject("get", 0).equalTo(singletonList("hello")).$(),
            asObject("get", 1).equalTo(singletonList("world")).$()
        )
    );
  }

  @Test
  public void group3() {
    SchemafulTupleSet data = JoinDataSet.load(3, 10);
    System.out.println(data.size());
    AtomicInteger sum = new AtomicInteger(0);
    Utils.group(data, 6).forEach(
        tuples -> {
          System.out.println("group:size=" + tuples.size());
          sum.getAndAdd(tuples.size());
          tuples.forEach(tuple -> System.out.println("  " + tuple));
        }
    );
    assertEquals(data.size(), sum.get());
  }
}
