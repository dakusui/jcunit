package com.github.dakusui.lisj.forms.control;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.LisjUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CondTest extends JCUnitBase {

  @Test
  public void cond_01() throws Exception {
    assertEquals(LisjUtils.bigDecimal(100),
        Basic.eval(this, cond(when(true, 100))));
  }

  @Test
  public void cond_02() throws Exception {
    assertEquals(LisjUtils.bigDecimal(100),
        Basic.eval(this, cond(when(true, 100), when(false, 200))));
  }

  @Test
  public void cond_03() throws Exception {
    assertEquals(LisjUtils.bigDecimal(100),
        Basic.eval(this, cond(when(false, 0), when(true, 100))));
  }

  @Test
  public void cond_04() throws Exception {
    assertEquals(LisjUtils.bigDecimal(0), Basic.eval(this,
        cond(when(true, 0), when(false, 100), when(false, 200))));
  }

  @Test
  public void cond_05() throws Exception {
    assertEquals(LisjUtils.bigDecimal(100), Basic.eval(this,
        cond(when(false, 0), when(true, 100), when(false, 200))));
  }

  @Test
  public void cond_06() throws Exception {
    assertEquals(LisjUtils.bigDecimal(200), Basic.eval(this,
        cond(when(false, 0), when(false, 100), when(true, 200))));
  }

  @Test
  public void cond_07() throws Exception {
    assertEquals(LisjUtils.bigDecimal(0),
        Basic
            .eval(this, cond(when(true, 0), when(true, 100), when(true, 200))));
  }

  @Test
  public void cond_08() throws Exception {
    assertEquals(
        LisjUtils.bigDecimal(0),
        Basic.eval(this,
            cond(when(same(123, 123), 0), when(false, 100), when(true, 200)))
    );
  }

  @Test
  public void cond_09() throws Exception {
    assertEquals(
        LisjUtils.bigDecimal(200),
        Basic.eval(this,
            cond(when(same(123, 124), 0), when(false, 100), when(true, 200)))
    );
  }

  @Test
  public void cond_10() throws Exception {
    assertEquals(
        false,
        Basic.eval(this,
            cond(when(same(123, 124), 0), when(false, 100), when(false, 200)))
    );
  }
}
