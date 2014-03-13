package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.lisj.Basic;

public class FormEvalTest extends DefaultRuleSetBuilder {
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
