package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Java extends JCUnitBase {
  @In
  public int x = 123;

  @In
  public int y;

  @Test
  public void get_01() throws Exception {
    assertEquals(Utils.bigDecimal(123), Basic.eval(this, get("x")));
  }

  @Test
  public void set_01() throws Exception {
    assertEquals(0, y);
    Basic.eval(this, set("y", intValue(456)));
    assertEquals(Utils.bigDecimal(456), Basic.eval(this, get("y")));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void set_02() throws Exception {
    assertEquals(0, y);
    Basic.eval(this, set("y", 789));
  }

}
