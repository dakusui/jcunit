package com.github.dakusui.petronia.examples;

import org.apache.commons.lang3.StringUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;

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
  public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this)
      .summarizer(summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Test
  public void test() {
    this.out = StringUtils.join(f);
  }
}
