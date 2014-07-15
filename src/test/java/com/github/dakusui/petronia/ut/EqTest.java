package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EqTest extends JCUnitBase {
  @Test
  public void eq_t01() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, eq(1, 1)));
  }

  @Test
  public void eq_t02() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, eq(null, null)));
  }

  @Test
  public void eq_t03() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, eq(new Object[0], Basic.NIL)));
  }

  @Test
  public void eq_t04() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, eq(new Object[0], new Object[0])));
  }

  @Test
  public void eq_f01() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, eq(1, 2)));
  }

  @Test
  public void eq_f02() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, eq(1, null)));
  }

  @Test
  public void eq_f03() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, eq(null, 1)));
  }
}
