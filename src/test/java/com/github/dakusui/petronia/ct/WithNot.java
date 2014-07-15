package com.github.dakusui.petronia.ct;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WithNot extends JCUnitBase {
  @Test
  public void not_is_01() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, not(is(1, 1))));
  }

  @Test
  public void not_is_02() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, 2))));
  }

  @Test
  public void not_is_03() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, null))));
  }

  @Test
  public void not_is_04() throws LisjCheckedException, CUT {
    assertEquals(false, Basic.eval(this, not(is(null, null))));
  }
}
