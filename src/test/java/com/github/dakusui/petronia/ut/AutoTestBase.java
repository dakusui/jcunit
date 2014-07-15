package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.petronia.examples.Calc;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class AutoTestBase extends JCUnitBase {
  @Rule
  public RuleSet rules = autoRuleSet(this)
      .summarizer(summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @In
  public int a;

  @In
  public int b;

  @Out
  public int c = 123;

  @Test
  public void test() {
    this.c = new Calc().calc(Calc.Op.plus, this.a, this.b);
  }
}
