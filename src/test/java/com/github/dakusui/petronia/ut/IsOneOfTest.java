package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IsOneOfTest extends JCUnitBase {
  @Test
  public void is_01() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, is(123, 123)));
  }

  @Test
  public void is_02() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(123, 100)));
  }

  @Test
  public void is_03() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, is(null, null)));
  }

  @Test
  public void is_04() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(123, null)));
  }

  @Test
  public void is_05() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(null, 123)));
  }

  @Test
  public void isoneof_01() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123)));
  }

  @Test
  public void isoneof_02() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100)));
  }

  @Test
  public void isoneof_11() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123, 100)));
  }

  @Test
  public void isoneof_12() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 100, 123)));
  }

  @Test
  public void isoneof_13() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100, 101)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void isoneof_e() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123)));
  }
}
