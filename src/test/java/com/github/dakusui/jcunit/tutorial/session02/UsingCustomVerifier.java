package com.github.dakusui.jcunit.tutorial.session02;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class UsingCustomVerifier {
  @Rule
  public RuleSet rules = new JCUnitBase()
      .autoRuleSet(this).summarizer(
          summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Out(
      verifier = CustomVerifier.class)
  public Exception e;

  @Test
  public void test() {
    e = new Exception("Hello");
  }
}
