package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.petronia.examples.Calc;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @FactorField
  public int     f1;
  @FactorField
  public int     f2;
  @FactorField
  public Calc.Op op;

  @Test
  public void test() {
    Calc calc = new Calc();
    System.out.println(String.format("f1=%d, op=%s, f2=%d = %d", f1, op, f2, calc.calc(this.op, f1, f2)));
  }
}
