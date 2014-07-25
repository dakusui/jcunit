package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.SchemafulTupleGeneration;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.MethodLevelsFactory;
import com.github.dakusui.jcunit.framework.examples.calc.Calc;
import com.github.dakusui.jcunit.generators.Generator;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @FactorField
  public int     f1;
  @FactorField(levelsFactory = MethodLevelsFactory.class)
  public int     f2;
  @FactorField
  private Calc.Op op;

  @FactorField(levelsFactory = SchemafulTupleLevelsFactory.class)
  @SchemafulTupleGeneration(
      generator = @Generator(
          value = IPO2SchemafulTupleGenerator.class,
          params = {@Param(type = Param.Type.Int, array = false, value = "2")}
      )
  )
  public Struct struct;

  public static int[] f2() {
    return new int[] { 1, 2, 3 };
  }

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
    if (e != null) {
      throw e;
    }
  }
}
