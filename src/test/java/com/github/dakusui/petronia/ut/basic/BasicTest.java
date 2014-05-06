package com.github.dakusui.petronia.ut.basic;

import junit.framework.TestCase;

import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class BasicTest extends JCUnitBase {
  @Test
  public void evalp_01() throws JCUnitException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), "100", "100")));
  }

  @Test
  public void evalp_02() throws JCUnitException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", 100)));
  }

  @Test
  public void evalp_03() throws JCUnitException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", "200")));
  }

  @Test
  public void evalp_04() throws JCUnitException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, 200)));
  }

  @Test
  public void evalp_05() throws JCUnitException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), null, null)));
  }

  @Test
  public void evalp_06() throws JCUnitException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, null)));
  }

  @Test
  public void evalp_07() throws JCUnitException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), null, 100)));
  }

}
