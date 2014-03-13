package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.lisj.Basic;

public class CompTest extends DefaultRuleSetBuilder {
  static class A implements Comparable<A> {
    String v;

    A(String v) {
      this.v = v;
    }

    @Override
    public int compareTo(A another) {
      return this.v.compareTo(another.v);
    }
  }

  static class B extends A {
    B(String v) {
      super(v);
    }
  }

  @Test
  public void lt() throws Exception {
    assertEquals(false, Basic.eval(this, lt(2, 1)));
    assertEquals(false, Basic.eval(this, lt(1, 1)));
    assertEquals(true, Basic.eval(this, lt(1, 2)));
  }

  @Test
  public void gt() throws Exception {
    assertEquals(true, Basic.eval(this, gt(2, 1)));
    assertEquals(false, Basic.eval(this, gt(1, 1)));
    assertEquals(false, Basic.eval(this, gt(1, 2)));
  }

  @Test
  public void gt_01() throws Exception {
    A a10 = new A("abc");
    A a11 = new A("abc");
    A a2 = new A("xyz");
    A b1 = new B("abc");
    A b2 = new B("xyz");

    assertEquals(true, Basic.eval(this, gt(a2, a10)));
    assertEquals(true, Basic.eval(this, gt(b2, a10)));
    assertEquals(false, Basic.eval(this, gt(a10, a10)));
    assertEquals(false, Basic.eval(this, gt(a10, a11)));
    assertEquals(false, Basic.eval(this, gt(a10, b1)));
    assertEquals(false, Basic.eval(this, gt(a10, a2)));
    assertEquals(false, Basic.eval(this, gt(a10, b2)));
    assertEquals(false, Basic.eval(this, gt(b1, a2)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void gt_err1() throws Exception {
    Basic.eval(this, gt(2, "ERR"));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void gt_err2() throws Exception {
    Basic.eval(this, gt(new A("ERR1"), "ERR1"));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void gt_err3() throws Exception {
    Basic.eval(this, gt(Object.class, Object.class));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void gt_err4() throws Exception {
    Basic.eval(this, gt("Hi", Object.class));
  }

  @Test
  public void le() throws Exception {
    assertEquals(false, Basic.eval(this, le(2, 1)));
    assertEquals(true, Basic.eval(this, le(1, 1)));
    assertEquals(true, Basic.eval(this, le(1, 2)));
  }

  @Test
  public void ge() throws Exception {
    assertEquals(true, Basic.eval(this, ge(2, 1)));
    assertEquals(true, Basic.eval(this, ge(1, 1)));
    assertEquals(false, Basic.eval(this, ge(1, 2)));
  }

  @Test
  public void same() throws Exception {
    assertEquals(false, Basic.eval(this, same(2, 1)));
    assertEquals(true, Basic.eval(this, same(1, 1)));
    assertEquals(false, Basic.eval(this, same(1, 2)));
  }

  @Test
  public void same_01() throws Exception {
    A a10 = new A("abc");
    A a11 = new A("abc");
    A a2 = new A("xyz");
    A b1 = new B("abc");
    A b2 = new B("xyz");

    assertEquals(false, Basic.eval(this, same(a2, a10)));
    assertEquals(true, Basic.eval(this, same(a10, a10)));
    assertEquals(true, Basic.eval(this, same(a10, a11)));
    assertEquals(true, Basic.eval(this, same(a10, b1)));
    assertEquals(false, Basic.eval(this, same(a10, a2)));
    assertEquals(false, Basic.eval(this, same(a10, b2)));
    assertEquals(false, Basic.eval(this, same(b1, a2)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void same_err1() throws Exception {
    Basic.eval(this, same(2, "ERR"));
  }

  @Test
  public void ne() throws Exception {
    assertEquals(true, Basic.eval(this, ne(2, 1)));
    assertEquals(false, Basic.eval(this, ne(1, 1)));
    assertEquals(true, Basic.eval(this, ne(1, 2)));
  }
}
