package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;
import com.github.dakusui.lisj.Basic;

public class LoopTest extends JCUnitBase {

  @Test
  public void loop_01() throws Exception {
    Basic.eval(this, assign($("i"), 0));
    Basic.eval(this,
        loop(lt($("i"), 10), print($("i")), assign($("i"), add($("i"), 1))));

    assertEquals(Utils.bigDecimal(10), Basic.eval(this, $("i")));
  }

  @Test
  public void loop_02() throws Exception {
    Basic.eval(
        this,
        progn(
            assign($("i"), 0),
            loop(lt($("i"), 10),
                print(format("@@@@@@@@ %s @@@@@@@@\n", $("i"))),
                assign($("i"), add($("i"), 1)))));

    assertEquals(Utils.bigDecimal(10), Basic.eval(this, $("i")));
  }

  @Test
  public void loop_03() throws Exception {
    Basic.eval(this, assign($("i"), 0));
    Basic.eval(this,
        loop(lt($("i"), 0), print($("i")), assign($("i"), add($("i"), 1))));
    assertEquals(Utils.bigDecimal(0), Basic.eval(this, $("i")));
  }

  @Test
  public void loop_04() throws Exception {
    Basic.eval(this, assign($("i"), 0));
    Basic.eval(this,
        loop(lt($("i"), 1), print($("i")), assign($("i"), add($("i"), 1))));
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, $("i")));
  }

  @Test(
      expected = JCUnitRuntimeException.class)
  public void loop_e01() throws Exception {
    // //
    // Test the behavior of 'loop' when the first parameter of it isn neither
    // predicate nor boolean.
    // But should it be a RuntimeException?
    Basic.eval(this, assign($("i"), 0));
    Basic.eval(
        this,
        loop(format("<%s>", lt($("i"), 1)), print($("i")),
            assign($("i"), add($("i"), 1))));
    assertEquals(Utils.bigDecimal(1), Basic.eval(this, $("i")));
  }
}
