package com.github.dakusui.lisj.forms.numeric;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.func.math.*;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class NumericFuncTest extends JCUnitBase {
  int    intnum    = 1;
  short  shortnum  = 1;
  long   longnum   = 1;
  byte   bytenum   = 1;
  float  floatnum  = 1.0f;
  double doublenum = 1.0d;

  @Test
  public void addint_00() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(intnum)));
  }

  @Test
  public void addint_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(intnum)));
  }

  @Test
  public void addint_02() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Add().bind(intnum, intnum)));
  }

  @Test
  public void addint_03() throws Exception {
    assertEquals(new BigDecimal(3),
        Basic.eval(this, new Add().bind(intnum, intnum, intnum)));
  }

  @Test
  public void addlong_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(longnum)));
  }

  @Test
  public void addlong_02() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Add().bind(longnum, longnum)));
  }

  @Test
  public void addlong_03() throws Exception {
    assertEquals(new BigDecimal(3),
        Basic.eval(this, new Add().bind(longnum, longnum, longnum)));
  }

  @Test
  public void addbyte_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(bytenum)));
  }

  @Test
  public void addbyte_02() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Add().bind(bytenum, bytenum)));
  }

  @Test
  public void addbyte_03() throws Exception {
    assertEquals(new BigDecimal(3),
        Basic.eval(this, new Add().bind(bytenum, bytenum, bytenum)));
  }

  @Test
  public void addfloat_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(floatnum)));
  }

  @Test
  public void addfloat_02() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Add().bind(floatnum, floatnum)));
  }

  @Test
  public void addfloat_03() throws Exception {
    assertEquals(new BigDecimal(3),
        Basic.eval(this, new Add().bind(floatnum, floatnum, floatnum)));
  }

  @Test
  public void adddouble_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Add().bind(floatnum)));
  }

  @Test
  public void adddouble_02() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Add().bind(doublenum, doublenum)));
  }

  @Test
  public void adddouble_03() throws Exception {
    assertEquals(new BigDecimal(3),
        Basic.eval(this, new Add().bind(doublenum, doublenum, doublenum)));
  }

  @Test
  public void subint_03() throws Exception {
    assertEquals(new BigDecimal(-1),
        Basic.eval(this, new Sub().bind(intnum, intnum, intnum)));
  }

  @Test
  public void mulint_03() throws Exception {
    assertEquals(new BigDecimal(8),
        Basic.eval(this, new Mul().bind(2, 2.0d, 2.0f)));
  }

  @Test
  public void divint_03() throws Exception {
    assertEquals(new BigDecimal(2),
        Basic.eval(this, new Div().bind(8, 2.0d, 2.0f)));
  }

  @Test
  public void max_01() throws Exception {
    assertEquals(new BigDecimal(3), Basic.eval(this, new Max().bind(1, 2, 3)));
  }

  @Test
  public void max_02() throws Exception {
    assertEquals(new BigDecimal(2), Basic.eval(this, new Max().bind(1, 2)));
  }

  @Test
  public void max_03() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Max().bind(1)));
  }

  @Test
  public void max_04() throws Exception {
    assertEquals(new BigDecimal(3), Basic.eval(this, new Max().bind(3, 1)));
  }

  @Test
  public void max_05() throws Exception {
    assertEquals(new BigDecimal(-1), Basic.eval(this, new Max().bind(-3, -1)));
  }

  @Test
  public void max_06() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Max().bind(-3, 1)));
  }

  @Test
  public void min_01() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Min().bind(1, 2)));
  }

  @Test
  public void min_02() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Min().bind(1, 2, 3)));
  }

  @Test
  public void min_03() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Min().bind(1)));
  }

  @Test
  public void min_04() throws Exception {
    assertEquals(new BigDecimal(1), Basic.eval(this, new Min().bind(3, 1)));
  }

  @Test
  public void min_05() throws Exception {
    assertEquals(new BigDecimal(-3), Basic.eval(this, new Min().bind(-3, 1)));
  }

  @Test
  public void min_06() throws Exception {
    assertEquals(new BigDecimal(-3), Basic.eval(this, new Min().bind(-3, -1)));
  }

  @Test
  public void invalid_01() throws Exception {
    try {
      Basic.eval(this, new Min().bind("hello", "world"));
      fail();
    } catch (ObjectUnderFrameworkException e) {
      assertTrue(true);
    }
  }
}
