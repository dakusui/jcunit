package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class Example extends JCUnitBase {
  @In(
      domain = Domain.Method)
  public int a;

  @In(
      domain = Domain.Method)
  public int b;

  @Out
  public int x;

  @Rule
  public RuleSet ruleSet = ruleSet().incase(
      isoneof(get("a"), 0, 1, 2),
      or(is(get("x"),
              add(get("a"), get("b"))),
          is(get("x"), 0)
      )
  )
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
