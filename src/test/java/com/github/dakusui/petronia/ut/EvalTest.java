package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EvalTest extends JCUnitBase {

  @Test
  public void eval_01() throws JCUnitCheckedException, CUT {
    assertEquals("Hello world", Basic.eval(this, eval("Hello world")));
  }

  @Test
  public void eval_02() throws JCUnitCheckedException, CUT {
    assertEquals(Basic.NIL, Basic.eval(this, eval(Basic.NIL)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void eval_03() throws JCUnitCheckedException, CUT {
    assertEquals(null, Basic.eval(this, eval()));
  }

  @Test
  public void eval_04() throws JCUnitCheckedException, CUT {
    assertEquals("hello", Basic.eval(this, eval(lambda($(), print("hello")))));
  }

  @Test
  public void eval_05() throws JCUnitCheckedException, CUT {
    assertEquals("world",
        Basic.eval(this, eval(lambda($(), print("hello"), print("world")))));
  }

  @Test
  public void eval_06() throws JCUnitCheckedException, CUT {
    assertEquals("world",
        Basic.eval(this, eval(print("hello"), print("world"))));
  }
}
