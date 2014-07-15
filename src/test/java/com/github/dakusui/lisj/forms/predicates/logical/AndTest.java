package com.github.dakusui.lisj.forms.predicates.logical;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AndTest extends JCUnitBase {
  @Test
  public void and_01() throws Exception {
    assertEquals(true, Basic.eval(this, and(true)));
  }

  @Test
  public void and_02() throws Exception {
    assertEquals(false, Basic.eval(this, and(false)));
  }

  @Test
  public void and_03() throws Exception {
    assertEquals(true, Basic.eval(this, and(true, true)));
  }

  @Test
  public void and_04() throws Exception {
    assertEquals(false, Basic.eval(this, and(true, false)));
  }

  @Test
  public void and_05() throws Exception {
    assertEquals(false, Basic.eval(this, and(false, true)));
  }

  @Test
  public void and_06() throws Exception {
    assertEquals(false, Basic.eval(this, and(false, false)));
  }

  @Test
  public void and_nested_1() throws Exception {
    assertEquals(true, Basic.eval(this, and(and(true, true))));
  }

  @Test
  public void and_nested_2() throws Exception {
    assertEquals(true, Basic.eval(this, and(and(true, true), true)));
  }


  @Test
  public void and_nested_3() throws Exception {
    assertEquals(true, Basic.eval(this, and(and(true, true), and(true, true))));
  }

  @Test
  public void and_nested_4() throws Exception {
    assertEquals(true, Basic.eval(this, and(and(true, true), and(true, true), and(true, true))));
  }

  @Test
  public void and_nested_b1() throws Exception {
    assertEquals(false, Basic.eval(this, and(and(true, false))));
  }

  @Test
  public void and_nested_b2() throws Exception {
    assertEquals(false, Basic.eval(this, and(and(true, true), false)));
  }


  @Test
  public void and_nested_b3() throws Exception {
    assertEquals(false, Basic.eval(this, and(and(true, true), and(true, false))));
  }

  @Test
  public void and_nested_b4() throws Exception {
    assertEquals(false, Basic.eval(this, and(and(true, true), and(true, true), and(true, false))));
  }

}
