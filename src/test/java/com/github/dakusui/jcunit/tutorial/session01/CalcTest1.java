package com.github.dakusui.jcunit.tutorial.session01;

import java.util.HashMap;
import java.util.Map;

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
/*
 * @Generator(CustomTestArrayGenerator.class)
 * 
 * @GeneratorParameters({
 * 
 * @Value(type = Type.IntArray, intArrayValue = { 0, 1, 2 }),
 * 
 * @Value(type = Type.IntArray, intArrayValue = { 0, 1, 2 }),
 * 
 * @Value(type = Type.IntArray, intArrayValue = { 0, 1, 2 }) })
 */
public class CalcTest1 {
  public static class Example {
    int a = 123;

    public int hashCode() {
      return a;
    }

    public boolean equals(Object another) {
      if (another == null)
        return false;
      if (!(another instanceof Example))
        return false;
      return this.a == ((Example) another).a;
    }
  }

  @In
  public int                 a;
  @In
  public int                 b;
  @Out
  public int                 c;
  @Out
  public Object              obj;
  @Out
  public String              str;
  @Out
  public Map<String, String> map        = new HashMap<String, String>();

  @Rule
  public RuleSet             rules      = new DefaultRuleSetBuilder()
                                            .autoRuleSet(this).summarizer(
                                                summarizer);

  @ClassRule
  public static Summarizer   summarizer = new BasicSummarizer();

  @Test
  public void test() {
    this.c = new Calc().calc(this.a, this.b);
    this.obj = new Example();
    ;
    this.str = "Hello";
    this.map.put("hi", "everyone!");
  }
}
