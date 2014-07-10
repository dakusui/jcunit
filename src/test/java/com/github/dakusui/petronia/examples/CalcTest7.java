package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters.Type;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters.Value;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.compat.generators.CustomTestArrayGenerator;
import com.github.dakusui.petronia.examples.Calc.Op;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(CustomTestArrayGenerator.class)
@GeneratorParameters({ @Value(
    type = Type.IntArray, intArrayValue = { 0, 1, 2 }), @Value(
    type = Type.IntArray, intArrayValue = { 1, 2, 3 }) })
public class CalcTest7 extends JCUnitBase {
  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();
  @Rule
  public        RuleSet    rules      = new JCUnitBase()
      .autoRuleSet(this).summarizer(
          summarizer);
  @In
  public int                        a;
  @In
  public int                        b;
  @In
  public Op                         op;
  @Out
  public int                        r;
  /*
   * A throwable object's equals method returns false when a de-serialized object
   * is given as a parameter.
   * So, I'm using it's class instead of instance itself.
   */
  @Out
  public Class<? extends Throwable> t;

  @Test
  public void test() {
    try {
      Calc calc = new Calc();
      r = calc.calc(op, a, b);
    } catch (RuntimeException e) {
      t = e.getClass();
    }
  }
}
