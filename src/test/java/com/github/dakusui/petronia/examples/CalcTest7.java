package com.github.dakusui.petronia.examples;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.core.GeneratorParameters.Type;
import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;
import com.github.dakusui.jcunit.generators.CustomTestArrayGenerator;
import com.github.dakusui.petronia.examples.Calc.Op;

@RunWith(JCUnit.class)
@Generator(CustomTestArrayGenerator.class)
@GeneratorParameters({ @Value(
    type = Type.IntArray, intArrayValue = { 0, 1, 2 }), @Value(
    type = Type.IntArray, intArrayValue = { 1, 2, 3 }) })
public class CalcTest7 extends DefaultRuleSetBuilder {
  @In
  public int a;
  @In
  public int b;
  @In
  public Op op;
  @Out
  public int r;
  @Out
  public Throwable t;

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Rule
  public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this)
      .summarizer(summarizer);

  @Test
  public void test() {
    try {
      Calc calc = new Calc();
      r = calc.calc(op, a, b);
    } catch (RuntimeException e) {
      t = e;
    }
  }
}
