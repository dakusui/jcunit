package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.UnmodifiableTuple;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnmodifiableTupleTest {
  Tuple             inner = new Tuple.Builder().put("a1", "v11").put("a2", "v21").build();
  UnmodifiableTuple sut   = new UnmodifiableTuple(inner);

  @Test
  public void size() {
    sut.size();
  }

  @Test
  public void isEmpty() {
    assertFalse(inner.isEmpty());
    assertEquals(inner.isEmpty(), sut.isEmpty());
  }

  @Test
  public void containsKey() {
    assertTrue(inner.containsKey("a1"));
    assertEquals(inner.containsKey("a1"), sut.containsKey("a1"));
  }

  @Test
  public void containsValue() {
    assertTrue(inner.containsValue("v11"));
    assertEquals(inner.containsValue("v11"), sut.containsValue("v11"));
  }

  @Test
  public void get() {
    assertEquals("v11", inner.get("a1"));
    assertEquals(inner.get("a1"), sut.get("a1"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void put() {
    sut.put("a3", "v31");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void remove() {
    sut.remove("a1");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void putAll() {
    sut.putAll(inner);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void clear() {
    sut.clear();
  }

  @Test
  public void keySet() {
    assertEquals(inner.keySet(), sut.keySet());
  }

  @Test
  public void values() {
    assertArrayEquals(inner.values().toArray(), sut.values().toArray());
  }

  @Test
  public void entrySet() {
    assertEquals(inner.entrySet(), sut.entrySet());
  }

  @Test
  public void cloneTuple() {
    assertTrue(sut.cloneTuple().isSubtupleOf(sut));
    assertTrue(sut.isSubtupleOf(sut.cloneTuple()));
  }

  @Test
  public void isSubtupleOf() {
    assertTrue(sut.isSubtupleOf(inner));
    assertFalse(sut.isSubtupleOf(new Tuple.Builder().build()));
  }

  @Test
  public void testToString() {
    assertEquals(inner.toString(), this.sut.toString());
  }
}
