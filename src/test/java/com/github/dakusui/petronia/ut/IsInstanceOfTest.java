package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertTrue;

public class IsInstanceOfTest extends JCUnitBase implements
    Serializable {
  private static final long serialVersionUID = 1L;

  @Test
  public void isinstanceof_01() throws LisjCheckedException, CUT {
    assertTrue((Boolean) Basic.eval(this, isinstanceof(this, Object.class)));
  }

  @Test
  public void isinstanceof_02() throws LisjCheckedException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(this, String.class)));
  }

  @Test
  public void isinstanceof_03() throws LisjCheckedException, CUT {
    assertTrue((Boolean) Basic.eval(this,
        isinstanceof(this, Serializable.class)));
  }

  @Test
  public void isinstanceof_04() throws LisjCheckedException, CUT {
    assertTrue(!(Boolean) Basic
        .eval(this, isinstanceof(this, Comparable.class)));
  }

  @Test
  public void isinstanceof_05() throws LisjCheckedException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(null, Object.class)));
  }

  @Test(
      expected = NullPointerException.class)
  public void isinstanceof_06() throws LisjCheckedException, CUT {
    assertTrue(!(Boolean) Basic.eval(this, isinstanceof(this, null)));
  }
}
