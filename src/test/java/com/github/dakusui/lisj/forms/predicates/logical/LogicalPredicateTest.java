package com.github.dakusui.lisj.forms.predicates.logical;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogicalPredicateTest extends JCUnitBase {
  @Test
  public void or00() throws Exception {
    assertEquals(false, Basic.eval(this, or()));
  }

  @Test
  public void or01() throws Exception {
    assertEquals(true, Basic.eval(this, or(true)));
  }

  @Test
  public void or02() throws Exception {
    assertEquals(false, Basic.eval(this, or(false)));
  }

  @Test
  public void or03() throws Exception {
    assertEquals(true, Basic.eval(this, or(true, false)));
  }

  @Test
  public void or04() throws Exception {
    assertEquals(true, Basic.eval(this, or(false, true)));
  }

  @Test
  public void or05() throws Exception {
    assertEquals(false, Basic.eval(this, or(false, false)));
  }

  @Test
  public void or11() throws Exception {
    assertEquals(true, Basic.eval(this, or(same(1, 2), same(3, 3))));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void or_err1() throws LisjCheckedException, CUT {
    Basic.eval(this, or("ERR"));
  }

  @Test
  public void or_err2() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, or(true, "ERR")));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void or_err3() throws LisjCheckedException, CUT {
    Basic.eval(this, or(false, "ERR"));
  }

  @Test
  public void and00() throws Exception {
    assertEquals(true, Basic.eval(this, and()));
  }

  @Test
  public void and01() throws Exception {
    assertEquals(true, Basic.eval(this, and(true)));
  }

  @Test
  public void and02() throws Exception {
    assertEquals(false, Basic.eval(this, and(false)));
  }

  @Test
  public void and03() throws Exception {
    assertEquals(false, Basic.eval(this, and(true, false)));
  }

  @Test
  public void and04() throws Exception {
    assertEquals(false, Basic.eval(this, and(false, true)));
  }

  @Test
  public void and05() throws Exception {
    assertEquals(false, Basic.eval(this, and(false, false)));
  }

  @Test
  public void and06() throws Exception {
    assertEquals(true, Basic.eval(this, and(true, true)));
  }

  @Test
  public void and11() throws Exception {
    assertEquals(false, Basic.eval(this, and(same(1, 2), same(3, 3))));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void and_err1() throws LisjCheckedException, CUT {
    Basic.eval(this, and("ERR"));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void and_err2() throws LisjCheckedException, CUT {
    Basic.eval(this, and(true, "ERR"));
  }

  @Test
  public void and_err3() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, and(false, "ERR")));
  }

  @Test
  public void not_01() throws Exception {
    assertEquals(false, Basic.eval(this, not(true)));
  }

  @Test
  public void not_02() throws Exception {
    assertEquals(true, Basic.eval(this, not(false)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void not_err() throws Exception {
    Basic.eval(this, not("ERR"));
  }
}
