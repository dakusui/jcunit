package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.petronia.examples.Calc;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @FactorField
  public int f1;
  @FactorField
  public int f2;

  @Test
  public void test() {
    Calc calc = new Calc();
    System.out.println(String.format("f1=%d,f2=%d", f1, f2));
    System.out.println(calc.calc(Calc.Op.plus, f1, f2));
  }
}
