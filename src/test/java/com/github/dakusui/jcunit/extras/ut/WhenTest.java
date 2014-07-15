package com.github.dakusui.jcunit.extras.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Test;

public class WhenTest extends JCUnitBase {
  @Test
  public void when_01() throws Exception {
    System.out.println(Basic.eval(this, when(true, 100)));
  }

  @Test(
      expected = CUT.class)
  public void when_02() throws Exception {
    System.out.println(Basic.eval(this, when(false, 100)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void when_03() throws Exception {
    System.out.println(Basic.eval(this, when("ERR!", 100)));
  }
}
