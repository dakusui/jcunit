package com.github.dakusui.petronia.ut;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class MaxMinTest extends DefaultRuleSetBuilder {
  @Test
  public void max_01() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(100), Basic.eval(this, max(100)));
  }

  @Test
  public void max_02() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(2), Basic.eval(this, max(1, 2)));
    assertEquals(Utils.bigDecimal(2), Basic.eval(this, max(2, 1)));
  }

  @Test
  public void max_03() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(3), Basic.eval(this, max(1, 2, 3)));
    assertEquals(Utils.bigDecimal(3), Basic.eval(this, max(2, 3, 1)));
    assertEquals(Utils.bigDecimal(3), Basic.eval(this, max(3, 2, 1)));
  }

  @Test
  public void max_minus() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(-1), Basic.eval(this, max(-1, -2)));
    assertEquals(Utils.bigDecimal(-1), Basic.eval(this, max(-2, -1)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void max_e() throws JCUnitException, CUT {
    Basic.eval(this, max());
  }

  @Test
  public void min_01() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(100), Basic.eval(this, min(100)));
  }

  @Test
  public void min_02() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, min(1, 2)));
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, min(2, 1)));
  }

  @Test
  public void min_03() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, min(1, 2, 3)));
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, min(2, 3, 1)));
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, min(3, 2, 1)));
  }

  @Test
  public void min_minus() throws JCUnitException, CUT {
    assertEquals(Utils.bigDecimal(-2), Basic.eval(this, min(-1, -2)));
    assertEquals(Utils.bigDecimal(-2), Basic.eval(this, min(-2, -1)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void min_e() throws JCUnitException, CUT {
    Basic.eval(this, min());
  }

}
