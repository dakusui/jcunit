package com.github.dakusui.petronia.ut.logical;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrTest extends JCUnitBase {
  @Test
  public void or_01() throws Exception {
    assertEquals(true, Basic.eval(this, or(true)));
  }

  @Test
  public void or_02() throws Exception {
    assertEquals(false, Basic.eval(this, or(false)));
  }

  @Test
  public void or_03() throws Exception {
    assertEquals(true, Basic.eval(this, or(true, true)));
  }


  @Test
  public void or_04() throws Exception {
    assertEquals(true, Basic.eval(this, or(true, false)));
  }

  @Test
  public void or_05() throws Exception {
    assertEquals(true, Basic.eval(this, or(false, true)));
  }


  @Test
  public void or_06() throws Exception {
    assertEquals(false, Basic.eval(this, or(false, false)));
  }

  @Test
  public void or_nested_1() throws Exception {
    assertEquals(false, Basic.eval(this, or(or(false, false))));
  }

  @Test
  public void or_nested_2() throws Exception {
    assertEquals(false, Basic.eval(this, or(false, or(false, false))));
  }


  @Test
  public void or_nested_3() throws Exception {
    assertEquals(false, Basic.eval(this, or(or(false, false), false)));
  }

  @Test
  public void or_nested_b1() throws Exception {
    assertEquals(true, Basic.eval(this, or(or(false, true))));
  }

  @Test
  public void or_nested_b2() throws Exception {
    assertEquals(true, Basic.eval(this, or(false, or(false, true))));
  }


  @Test
  public void or_nested_b3() throws Exception {
    assertEquals(true, Basic.eval(this, or(or(false, false), true)));
  }
}
