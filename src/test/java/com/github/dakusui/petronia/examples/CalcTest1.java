package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.petronia.examples.Calc.Op;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class CalcTest1 extends JCUnitBase {
  @In
  public int       a;
  @In
  public int       b;
  @In
  public Op        op;
  @Out
  public int       r;
  @Out
  public Throwable t;

  @Rule
  public RuleSet rules2 = ruleSet()
      .incase(
          any(),
          progn(print("*** H E L L O ***"),
              true)
      ).otherwise(true)
      .summarizer(summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Test
  public void test() {
    try {
      Calc calc = new Calc();
      r = calc.calc(op, a, b);
      // LOGGER.info("result:" + (r) + " = " + a + " " + op.str() + " " + b);
    } catch (RuntimeException e) {
      t = e;
    }
  }
}
