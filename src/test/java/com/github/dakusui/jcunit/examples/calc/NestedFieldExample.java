package com.github.dakusui.jcunit.examples.calc;

import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.levelsproviders.SimpleLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.validator.ValidateWith;

@RunWith(JCUnit.class)
@ValidateWith(GenerateCoveringArrayWith.Validator.class)
public class NestedFieldExample extends Metatest {
  public NestedFieldExample() {
    super(
        31, // tests,
        0,  // failed
        0   // ignored
    );
  }

  @GenerateCoveringArrayWith(
      engine = @Generator(
          value = IPO2CoveringArrayEngine.class,
          configValues = @Value("2")
      ))
  public static class Struct {
    @FactorField(intLevels = { 123, 456 })
    public int    a;
    @FactorField(stringLevels = { "A", "B", "C" })
    public String b;

    public String toString() {
      return a + ";" + b;
    }
  }

  @Rule
  public TestDescription testDesc = new TestDescription();

  @FactorField(levelsProvider = LevelsProvider$f1.class)
  public int f1;

  public static class LevelsProvider$f1 extends SimpleLevelsProvider {
    @Override
    protected Object[] values() {
      return new Object[] { 1, 2, 3 };
    }
  }

  @FactorField(levelsProvider = LevelsProvider$f2.class)
  public long f2;

  public static class LevelsProvider$f2 extends SimpleLevelsProvider {
    @Override
    protected Object[] values() {
      return new Object[] { 1L, 2L, 3L };
    }
  }

  @FactorField
  public Calc.Op op;

  @SuppressWarnings("unused") // This field is used by JCUnit.
  @FactorField
  public Struct struct;

  @Test
  public void test() throws Exception {
    Calc calc = new Calc();
    String result = "(N/A)";
    try {
      result = Integer.toString(calc.calc(this.op, f1, (int) f2));
    } finally {
      UTUtils.stdout().println(
          StringUtils.format(
              "testname=%s: f1=%s, op=%s, f2=%s = %s ; struct = (%s)",
              this.testDesc.getTestName(),
              f1,
              op,
              f2,
              result,
              struct));
    }
  }
}
