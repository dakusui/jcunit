package com.github.dakusui.lisj.basic;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import junit.framework.TestCase;
import org.junit.Test;

public class BasicTest extends JCUnitBase {
  @Test
  public void evalp_01() throws LisjCheckedException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), "100", "100")));
  }

  @Test
  public void evalp_02() throws LisjCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", 100)));
  }

  @Test
  public void evalp_03() throws LisjCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), "100", "200")));
  }

  @Test
  public void evalp_04() throws LisjCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, 200)));
  }

  @Test
  public void evalp_05() throws LisjCheckedException, CUT {
    TestCase.assertTrue(Basic.evalp(this, Basic.quote($("eq"), null, null)));
  }

  @Test
  public void evalp_06() throws LisjCheckedException, CUT {
    TestCase.assertFalse(Basic.evalp(this, Basic.quote($("eq"), 100, null)));
  }

  @Test
  public void evalp_07() throws LisjCheckedException, CUT {
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
