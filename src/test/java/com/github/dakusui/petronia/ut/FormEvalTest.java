package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormEvalTest extends JCUnitBase {
  @Test
  public void eval01() throws Exception {
    assertEquals("hello", Basic.eval(this, "hello"));
  }

  @Test
  public void eval02() throws Exception {
    Basic.eval(this, assign($("var1"), "howdy"));
    assertEquals("howdy", Basic.eval(this, $("var1")));
  }

  @Test
  public void eval09() throws Exception {
    Basic.eval(this, Basic.arr("print", System.out, "hello, world\n"));
  }
}
