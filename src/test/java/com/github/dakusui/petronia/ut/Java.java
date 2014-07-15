package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.LisjUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Java extends JCUnitBase {
  @In
  public int x = 123;

  @In
  public int y;

  @Test
  public void get_01() throws Exception {
    assertEquals(LisjUtils.bigDecimal(123), Basic.eval(this, get("x")));
  }

  @Test
  public void set_01() throws Exception {
    assertEquals(0, y);
    Basic.eval(this, set("y", intValue(456)));
    assertEquals(LisjUtils.bigDecimal(456), Basic.eval(this, get("y")));
  }

  @Test(
      expected = JCUnitException.class)
  public void set_02() throws Exception {
    assertEquals(0, y);
    Basic.eval(this, set("y", 789));
  }

}
