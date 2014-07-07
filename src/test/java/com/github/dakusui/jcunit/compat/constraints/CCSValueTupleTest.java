package com.github.dakusui.jcunit.compat.constraints;

import com.github.dakusui.jcunit.constraints.constraintmanagers.ccs.CCSValueTupleSet;
import com.github.dakusui.jcunit.core.Tuple;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CCSValueTupleTest {
  @Test
  public void test1() {
    Tuple tuple1 = new Tuple();
    Tuple tuple2 = new Tuple();

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(0, merged.size());
  }

  @Test
  public void test2() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    Tuple tuple2 = new Tuple();

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is((Object) "V11"));
  }

  @Test
  public void test3() {
    Tuple tuple1 = new Tuple();
    Tuple tuple2 = new Tuple();
    tuple2.put("A1", "V11");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is((Object) "V11"));
  }

  @Test
  public void test4() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    Tuple tuple2 = new Tuple();
    tuple2.put("A1", "V11");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is((Object) "V11"));
  }

  @Test
  public void test5() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    Tuple tuple2 = new Tuple();
    tuple2.put("A2", "V21");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is((Object) "V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is((Object) "V21"));
  }

  @Test
  public void test6() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    Tuple tuple2 = new Tuple();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V21");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is((Object) "V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is((Object) "V21"));
  }

  @Test
  public void inconsistent() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    Tuple tuple2 = new Tuple();
    tuple2.put("A1", "V12");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(null, merged);
  }

  @Test
  public void inconsistent2() {
    Tuple tuple1 = new Tuple();
    tuple1.put("A1", "V11");
    tuple1.put("A2", "V21");
    Tuple tuple2 = new Tuple();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V22");

    Tuple merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(merged, null);
  }
}
