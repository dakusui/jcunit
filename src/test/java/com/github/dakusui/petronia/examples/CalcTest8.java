package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.generators.IPO2TestArrayGenerator;
import com.github.dakusui.petronia.examples.Calc.Op;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(IPO2TestArrayGenerator.class)
public class CalcTest8 extends JCUnitBase {
  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();
  @Rule
  public        RuleSet    rules      = new JCUnitBase().autoRuleSet(this)
      .summarizer(summarizer);
  @In
  public int                        a;
  @In
  public int                        b;
  @In
  public Op                         op;
  @Out
  public int                        r;
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
