package com.github.dakusui.lisj.forms.str;

import com.github.dakusui.lisj.Basic;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import static com.github.dakusui.lisj.Basic.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Cons2Test {
  @Test
  public void test_91() {
    Object obj = cons(1, cons(cons(2, 3), NIL));
    assertEquals(1, Basic.get(obj, 0));
    System.out.println(ArrayUtils.toString(obj));
    System.out.println(Basic.tostr(obj));
    System.out.println(Basic.get(obj, 0));
    System.out.println(ArrayUtils.toString(Basic.get(obj, 1)));
    assertTrue(eq(cons(2, 3), Basic.get(obj, 1)));
    assertEquals(2, length(obj));
    assertEquals("1", Basic.tostr(Basic.get(obj, 0)));
    assertEquals("(2,3)", Basic.tostr(Basic.get(obj, 1)));
  }

  @Test
  public void test_92a() {
    Object obj = new Object[] { 1, 2, new Object[] { 3 } };
    System.out.println(ArrayUtils.toString(obj));
    assertEquals("(1,2,3)", Basic.tostr(obj));
    assertEquals(3, Basic.length(obj));
    assertEquals("1", Basic.tostr(Basic.get(obj, 0)));
    assertEquals("2", Basic.tostr(Basic.get(obj, 1)));
    assertEquals("3", Basic.tostr(Basic.get(obj, 2)));
  }

  @Test
  public void test_92b() {
    Object obj = new Object[] { new Object[] { 1, 2, 3 } };
    assertEquals("((1,2,3))", Basic.tostr(obj));
    assertEquals(1, Basic.length(obj));
    assertEquals("(1,2,3)", Basic.tostr(Basic.get(obj, 0)));
  }

  @Test
  public void test_93() {
    Object obj = new Object[] { 1, 2, new Object[] { 3, 4 } };
    assertEquals("(1,2,3,4)", Basic.tostr(obj));
    assertEquals(4, Basic.length(obj));
    assertEquals("1", Basic.tostr(Basic.get(obj, 0)));
    assertEquals("2", Basic.tostr(Basic.get(obj, 1)));
    assertEquals("3", Basic.tostr(Basic.get(obj, 2)));
    assertEquals("4", Basic.tostr(Basic.get(obj, 3)));
  }

  @Test
  public void test_94() {
    Object obj = new Object[] { 1, new Object[] { 2, 3 } };
    assertEquals("(1,2,3)", Basic.tostr(obj));
    assertEquals(3, Basic.length(obj));
    assertEquals("1", Basic.tostr(Basic.get(obj, 0)));
    assertEquals("2", Basic.tostr(Basic.get(obj, 1)));
    assertEquals("3", Basic.tostr(Basic.get(obj, 2)));
  }

  @Test
  public void test_95() {
    Object obj = new Object[] { 1 };
    assertEquals("(1)", Basic.tostr(obj));
    assertEquals(1, Basic.length(obj));
    assertEquals("1", Basic.tostr(Basic.get(obj, 0)));
  }

  @Test
  public void test_96() {
    Object obj = new Object[] { new Object[] { 1 } };
    assertEquals("((1))", Basic.tostr(obj));
    assertEquals(1, Basic.length(obj));
    assertEquals("(1)", Basic.tostr(Basic.get(obj, 0)));
  }

  @Test
  public void test_97() {
    Object obj = new Object[] { };
    assertEquals("NIL", Basic.tostr(obj));
    assertEquals(0, Basic.length(obj));
  }

  @Test
  public void test_98() {
    Object obj = new Object[] {
        "format",
        new Object[] { "-%s-",
            new Object[] { new Object[] { "intValue", 123 } } } };
    System.out.println(Basic.tostr(obj));
    System.out.println(ArrayUtils.toString(obj));
  }

}
