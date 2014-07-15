package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.MethodLevelsFactory;
import com.github.dakusui.jcunit.extras.examples.Calc;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @FactorField
  public int     f1;
  @FactorField(levelsFactory = MethodLevelsFactory.class)
  public int     f2;
  public static int[] f2() {
    return new int[]{1,2,3};
  }
  @FactorField
  public Calc.Op op;

  @Test
  public void test() throws Exception {
    Calc calc = new Calc();
    String result = "(N/A)";
    Exception e = null;
    try {
      result = Integer.toString(calc.calc(this.op, f1, f2));
    } catch (Exception ee) {
      e = ee;
    }
    System.out.println(String.format("f1=%d, op=%s, f2=%d = %s", f1, op, f2,
        result));
    if (e != null) throw e;
  }
}
