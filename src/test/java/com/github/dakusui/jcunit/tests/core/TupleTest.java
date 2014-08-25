package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TupleTest {
  @Test
  public void isSubtupleOf01() {
    assertThat(
        new Tuple.Builder().build().isSubtupleOf(
            new Tuple.Builder().put("A", "a1").build()
        ),
        is(true)
    );
  }

  @Test
  public void isSubtupleOf02() {
    assertThat(
        new Tuple.Builder().put("A", "a1").build().isSubtupleOf(
            new Tuple.Builder().put("A", "a1").build()
        ),
        is(true)
    );
  }

  @Test
  public void isSubtupleOf03() {
    assertThat(
        new Tuple.Builder().put("A", "a1").build().isSubtupleOf(
            new Tuple.Builder().put("A", "a1").put("B", "b1").build()
        ),
        is(true)
    );
  }

}
