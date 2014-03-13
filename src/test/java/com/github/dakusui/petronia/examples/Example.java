package com.github.dakusui.petronia.examples;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.BasicSummarizer;

@RunWith(JCUnit.class)
public class Example extends DefaultRuleSetBuilder {
  @In(
      domain = Domain.Method)
  public int                    a;

  @In(
      domain = Domain.Method)
  public int                    b;

  @Out
  public int                    x;

  @Rule
  public RuleSet                ruleSet    = ruleSet().incase(
                                               isoneof(get("a"), 0, 1, 2),
                                               or(is(get("x"),
                                                   add(get("a"), get("b"))),
                                                   is(get("x"), 0)))
                                               .summarizer(summarizer);

  @ClassRule
  public static BasicSummarizer summarizer = new BasicSummarizer();

  static int[] a() {
    return new int[] { 0, 1, 2 };
  }

  static int[] b() {
    return new int[] { 0, 1, 2 };
  }

  @Test
  public void test() {
    x = a + b;
    System.out.printf("a=%d, b=%d, x=%d\n", a, b, x);
  }
}
