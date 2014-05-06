package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class IsInstanceOfTest extends JCUnitBase implements
    Serializable {
  private static final long serialVersionUID = 1L;

  @Test
  public void isinstanceof_01() throws JCUnitException, CUT {
    assertTrue((Boolean) Basic.eval(this, isinstanceof(this, Object.class)));
  }

  @Test
  public void isinstanceof_02() throws JCUnitException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(this, String.class)));
  }

  @Test
  public void isinstanceof_03() throws JCUnitException, CUT {
    assertTrue((Boolean) Basic.eval(this,
        isinstanceof(this, Serializable.class)));
  }

  @Test
  public void isinstanceof_04() throws JCUnitException, CUT {
    assertTrue(!(Boolean) Basic
        .eval(this, isinstanceof(this, Comparable.class)));
  }

  @Test
  public void isinstanceof_05() throws JCUnitException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(null, Object.class)));
  }

  @Test(
      expected = NullPointerException.class)
  public void isinstanceof_06() throws JCUnitException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(this, null)));
  }
}
