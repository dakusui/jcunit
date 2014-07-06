package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.constraints.ccs.CCSValueTupleSet;
import com.github.dakusui.jcunit.core.ValueTuple;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CCSValueTupleTest {
  @Test
  public void test1() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(0, merged.size());
  }

  @Test
  public void test2() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test3() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A1", "V11");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test4() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A1", "V11");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test5() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A2", "V21");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is("V21"));
  }

  @Test
  public void test6() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V21");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is("V21"));
  }

  @Test
  public void inconsistent() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A1", "V12");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(null, merged);
  }

  @Test
  public void inconsistent2() {
    ValueTuple<String, String> tuple1 = new ValueTuple<String, String>();
    tuple1.put("A1", "V11");
    tuple1.put("A2", "V21");
    ValueTuple<String, String> tuple2 = new ValueTuple<String, String>();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V22");

    ValueTuple<String, String> merged = CCSValueTupleSet.merge(tuple1, tuple2);

    assertEquals(merged, null);
  }
}
