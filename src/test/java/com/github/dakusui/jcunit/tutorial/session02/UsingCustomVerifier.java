package com.github.dakusui.jcunit.tutorial.session02;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;

@RunWith(JCUnit.class)
public class UsingCustomVerifier {
  @Rule
  public RuleSet           rules      = new DefaultRuleSetBuilder()
                                          .autoRuleSet(this).summarizer(
                                              summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Out(
      verifier = CustomVerifier.class)
  public Exception         e;

  @Test
  public void test() {
    e = new Exception("Hello");
  }
}
