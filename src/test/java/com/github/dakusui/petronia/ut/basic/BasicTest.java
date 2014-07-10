package com.github.dakusui.petronia.ut.basic;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import junit.framework.TestCase;
import org.junit.Test;

public class BasicTest extends JCUnitBase {
  @Test
  public void evalp_01() throws JCUnitCheckedException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), "100", "100")));
  }

  @Test
  public void evalp_02() throws JCUnitCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", 100)));
  }

  @Test
  public void evalp_03() throws JCUnitCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", "200")));
  }

  @Test
  public void evalp_04() throws JCUnitCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, 200)));
  }

  @Test
  public void evalp_05() throws JCUnitCheckedException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), null, null)));
  }

  @Test
  public void evalp_06() throws JCUnitCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, null)));
  }

  @Test
  public void evalp_07() throws JCUnitCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), null, 100)));
  }

  ////
  // A known bug.
  // @Test()
  public void eq_01() throws Exception {
    TestCase.assertEquals(true, Basic
        .eq(new Object[] { 1, new Object[] { 1 } }, new Object[] { 1, 1 }));
  }
}
