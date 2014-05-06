package com.github.dakusui.petronia.ut;

import static com.github.dakusui.lisj.Basic.car;
import static com.github.dakusui.lisj.Basic.cdr;
import static com.github.dakusui.lisj.Basic.cons;
import static com.github.dakusui.lisj.Basic.tostr;
import static org.junit.Assert.assertEquals;

import java.util.IllegalFormatException;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;

public class FormatTest extends JCUnitBase {
  @Test
  public void format_01() throws Exception {
    System.out.println(tostr(eval(this, format("-%s-", intValue(1)))));
    // assertEquals("-1-", Basic.eval(this, format("-%s-", intValue(1))));
  }

  @Test
  public void format_02() throws Exception {
    // System.out.println("==>" + Basic.eval(this, format("-%s-",
    // bigInteger(1))));
    assertEquals("-1-", Basic.eval(this, format("-%s-", bigInteger(1))));
  }

  @Test(
      expected = IllegalFormatException.class)
  public void format_02e() throws Exception {
    Basic.eval(this, format("-%d-", bigDecimal(1)));
  }

  @Test
  public void format_03() throws Exception {
    assertEquals("-1.0-", Basic.eval(this, format("-%1.1f-", bigDecimal(1))));
  }

  @Test
  public void format_04() throws Exception {
    // 2 arguments test
    assertEquals("*hello*world*everyone*",
        Basic.eval(this, format("*%s*%s*%s*", "hello", "world", "everyone")));
  }

  public static void main(String[] args) {
    Object cons = cons("format",
        new Object[] { "-%d-", cons("bigInteger", new Object[] { 1 }) });
    System.out.println(Basic.tostr(cons));
    System.out.println(ArrayUtils.toString(cons));
    System.out.println(Basic.tostr(car(cons)));
    System.out.println(ArrayUtils.toString(car(cons)));
    System.out.println(Basic.tostr(cdr(cons)));
    System.out.println(ArrayUtils.toString(cdr(cons)));
  }
}
