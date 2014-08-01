package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.MethodLevelsFactory;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.framework.examples.calc.Calc;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @Rule
  public JCUnitDesc testDesc = new JCUnitDesc();

  @FactorField(levelsFactory = MethodLevelsFactory.class)
  public  int     f1;
  @FactorField(levelsFactory = MethodLevelsFactory.class)
  public  long    f2;
  @FactorField
  private Calc.Op op;

  @FactorField(levelsFactory = SchemafulTupleLevelsFactory.class)
  @SchemafulTupleGeneration(
      generator = @Generator(
          value = IPO2SchemafulTupleGenerator.class,
          params = { @Param(type = Param.Type.Int, array = false, value = "2") }
      ))
  private Struct struct;

  public static int[] f1() {
    return new int[] { 1, 2, 3 };
  }

  public static long[] f2() {
    return new long[] { 1, 2, 3 };
  }

  @Test
  public void test() throws Exception {
    Calc calc = new Calc();
    String result = "(N/A)";
    Exception e = null;
    try {
      result = Integer.toString(calc.calc(this.op, f1, (int) f2));
    } catch (Exception ee) {
      e = ee;
    }
    System.out.println(testDesc.getTestName());

    System.out.println(
        String.format(
            "f1=%d, op=%s, f2=%d = %s ; struct = (%s)",
            f1,
            op,
            f2,
            result,
            struct));
    if (e != null) {
      throw e;
    }
  }
}
