package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class IsOneOfTest extends DefaultRuleSetBuilder {
  @Test
  public void is_01() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, is(123, 123)));
  }

  @Test
  public void is_02() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, is(123, 100)));
  }

  @Test
  public void is_03() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, is(null, null)));
  }

  @Test
  public void is_04() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, is(123, null)));
  }

  @Test
  public void is_05() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, is(null, 123)));
  }

  @Test
  public void isoneof_01() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123)));
  }

  @Test
  public void isoneof_02() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100)));
  }

  @Test
  public void isoneof_11() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123, 100)));
  }

  @Test
  public void isoneof_12() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 100, 123)));
  }

  @Test
  public void isoneof_13() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100, 101)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void isoneof_e() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123)));
  }
}
