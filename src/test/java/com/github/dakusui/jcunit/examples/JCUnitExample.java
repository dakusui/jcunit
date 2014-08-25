package com.github.dakusui.jcunit.examples;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.MethodLevelsProvider;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.examples.calc.Calc;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @Rule
  public JCUnitDesc testDesc = new JCUnitDesc();

  @FactorField(levelsFactory = MethodLevelsProvider.class)
  public int     f1;
  @FactorField(levelsFactory = MethodLevelsProvider.class)
  public long    f2;
  @FactorField
  public Calc.Op op;

  @SuppressWarnings("unused") // This field is used by JCUnit.
  @FactorField(levelsFactory = TupleLevelsProvider.class)
  @TupleGeneration(
      generator = @Generator(
          value = IPO2TupleGenerator.class,
          params = @Param("2")
      ))
  public Struct struct;

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
