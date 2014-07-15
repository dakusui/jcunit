package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxMinTest extends JCUnitBase {
  @Test
  public void max_01() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(100), Basic.eval(this, max(100)));
  }

  @Test
  public void max_02() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(2), Basic.eval(this, max(1, 2)));
    assertEquals(LisjUtils.bigDecimal(2), Basic.eval(this, max(2, 1)));
  }

  @Test
  public void max_03() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(3), Basic.eval(this, max(1, 2, 3)));
    assertEquals(LisjUtils.bigDecimal(3), Basic.eval(this, max(2, 3, 1)));
    assertEquals(LisjUtils.bigDecimal(3), Basic.eval(this, max(3, 2, 1)));
  }

  @Test
  public void max_minus() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(-1), Basic.eval(this, max(-1, -2)));
    assertEquals(LisjUtils.bigDecimal(-1), Basic.eval(this, max(-2, -1)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void max_e() throws LisjCheckedException, CUT {
    Basic.eval(this, max());
  }

  @Test
  public void min_01() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(100), Basic.eval(this, min(100)));
  }

  @Test
  public void min_02() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(1), Basic.eval(this, min(1, 2)));
    assertEquals(LisjUtils.bigDecimal(1), Basic.eval(this, min(2, 1)));
  }

  @Test
  public void min_03() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(1), Basic.eval(this, min(1, 2, 3)));
    assertEquals(LisjUtils.bigDecimal(1), Basic.eval(this, min(2, 3, 1)));
    assertEquals(LisjUtils.bigDecimal(1), Basic.eval(this, min(3, 2, 1)));
  }

  @Test
  public void min_minus() throws LisjCheckedException, CUT {
    assertEquals(LisjUtils.bigDecimal(-2), Basic.eval(this, min(-1, -2)));
    assertEquals(LisjUtils.bigDecimal(-2), Basic.eval(this, min(-2, -1)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void min_e() throws LisjCheckedException, CUT {
    Basic.eval(this, min());
  }

}
