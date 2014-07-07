package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssignTest extends JCUnitBase {

  @Test
  public void assign_01() throws Exception {
    Basic.eval(this, assign($("x"), 100));

    assertEquals(Utils.bigDecimal(100), Basic.eval(this, $("x")));
  }

  @Test
  public void assign_02() throws Exception {
    Basic.eval(this, assign($("x"), 100));
    Basic.eval(this, assign($("x"), add($("x"), 1)));

    assertEquals(Utils.bigDecimal(101), Basic.eval(this, $("x")));
  }
}
