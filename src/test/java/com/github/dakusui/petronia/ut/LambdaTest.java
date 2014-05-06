package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;

public class LambdaTest extends JCUnitBase {
  @Test
  public void lambda_00() throws Exception {
    /*
     * Test 0 parameter function.
     */
    Basic.eval(this, assign($("printit"), lambda($(), print("howdy!\n"))));
    assertEquals("howdy!\n", Basic.eval(this, new Object[] { $("printit") }));
  }

  @Test
  public void lambda_01() throws Exception {
    /*
     * Test 1 parameter function.
     */
    Basic.eval(this,
        assign($("printit"), lambda($("a"), print(format("%s!\n", $("a"))))));
    assertEquals("hello!\n",
        Basic.eval(this, new Object[] { $("printit"), "hello" }));
  }

  @Test
  public void lambda_02() throws Exception {
    /*
     * Test 2 parameters function.
     */
    Basic
        .eval(
            this,
            assign(
                $("printit"),
                lambda($("a", "b"),
                    print(format("Hi, %s! %s\n", $("a"), $("b"))))));
    assertEquals("Hi, Mayu! hello\n",
        Basic.eval(this, new Object[] { $("printit"), "Mayu", "hello" }));
  }
}
