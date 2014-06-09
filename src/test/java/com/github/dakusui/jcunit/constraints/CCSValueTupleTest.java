package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.constraints.ccs.CCSValueTuple;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CCSValueTupleTest {
  @Test
  public void test1() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertEquals(0, merged.size());
  }

  @Test
  public void test2() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test3() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A1", "V11");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test4() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A1", "V11");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertThat(merged.size(), is(1));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
  }

  @Test
  public void test5() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A2", "V21");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is("V21"));
  }

  @Test
  public void test6() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V21");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertThat(merged.size(), is(2));
    assertThat(merged.containsKey("A1"), is(true));
    assertThat(merged.get("A1"), is("V11"));
    assertThat(merged.containsKey("A2"), is(true));
    assertThat(merged.get("A2"), is("V21"));
  }

  @Test
  public void inconsistent() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A1", "V12");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertEquals(null, merged);
  }

  @Test
  public void inconsistent2() {
    CCSValueTuple<String, String> tuple1 = new CCSValueTuple<String, String>();
    tuple1.put("A1", "V11");
    tuple1.put("A2", "V21");
    CCSValueTuple<String, String> tuple2 = new CCSValueTuple<String, String>();
    tuple2.put("A1", "V11");
    tuple2.put("A2", "V22");

    CCSValueTuple<String, String> merged = tuple1.merge(tuple2);

    assertEquals(merged, null);
  }
}
