package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class EvalTest extends DefaultRuleSetBuilder {

  @Test
  public void eval_01() throws JCUnitException, CUT {
    assertEquals("Hello world", Basic.eval(this, eval("Hello world")));
  }

  @Test
  public void eval_02() throws JCUnitException, CUT {
    assertEquals(Basic.NIL, Basic.eval(this, eval(Basic.NIL)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void eval_03() throws JCUnitException, CUT {
    assertEquals(null, Basic.eval(this, eval()));
  }

  @Test
  public void eval_04() throws JCUnitException, CUT {
    assertEquals("hello", Basic.eval(this, eval(lambda($(), print("hello")))));
  }

  @Test
  public void eval_05() throws JCUnitException, CUT {
    assertEquals("world",
        Basic.eval(this, eval(lambda($(), print("hello"), print("world")))));
  }

  @Test
  public void eval_06() throws JCUnitException, CUT {
    assertEquals("world",
        Basic.eval(this, eval(print("hello"), print("world"))));
  }
}
