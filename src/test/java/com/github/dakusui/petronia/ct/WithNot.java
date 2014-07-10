package com.github.dakusui.petronia.ct;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WithNot extends JCUnitBase {
  @Test
  public void not_is_01() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, not(is(1, 1))));
  }

  @Test
  public void not_is_02() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, 2))));
  }

  @Test
  public void not_is_03() throws JCUnitCheckedException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, null))));
  }

  @Test
  public void not_is_04() throws JCUnitCheckedException, CUT {
    assertEquals(false, Basic.eval(this, not(is(null, null))));
  }
}
