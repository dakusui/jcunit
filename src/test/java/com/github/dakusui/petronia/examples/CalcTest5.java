package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Out;
import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class CalcTest5 {
  @In(
      domain = In.Domain.Method)
  public String[] f;

  public static String[][] f() {
    return new String[][] { new String[] { "hello", "world" } };
  }

  @Out
  public String out;

  @Rule
  public RuleSet rules = new JCUnitBase()
      .autoRuleSet(this).summarizer(
          summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Test
  public void test() {
    this.out = StringUtils.join(f);
  }
}
