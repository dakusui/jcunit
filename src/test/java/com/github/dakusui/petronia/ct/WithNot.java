package com.github.dakusui.petronia.ct;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class WithNot extends DefaultRuleSetBuilder {
  @Test
  public void not_is_01() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, not(is(1, 1))));
  }

  @Test
  public void not_is_02() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, 2))));
  }

  @Test
  public void not_is_03() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, not(is(1, null))));
  }

  @Test
  public void not_is_04() throws JCUnitException, CUT {
    assertEquals(false, Basic.eval(this, not(is(null, null))));
  }
}
