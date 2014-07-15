package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IsOneOfTest extends JCUnitBase {
  @Test
  public void is_01() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, is(123, 123)));
  }

  @Test
  public void is_02() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(123, 100)));
  }

  @Test
  public void is_03() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, is(null, null)));
  }

  @Test
  public void is_04() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(123, null)));
  }

  @Test
  public void is_05() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, is(null, 123)));
  }

  @Test
  public void isoneof_01() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123)));
  }

  @Test
  public void isoneof_02() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100)));
  }

  @Test
  public void isoneof_11() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 123, 100)));
  }

  @Test
  public void isoneof_12() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, isoneof(123, 100, 123)));
  }

  @Test
  public void isoneof_13() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123, 100, 101)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void isoneof_e() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, isoneof(123)));
  }
}
