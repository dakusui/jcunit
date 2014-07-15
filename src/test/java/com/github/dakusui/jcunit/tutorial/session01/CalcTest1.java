package com.github.dakusui.jcunit.tutorial.session01;

import com.github.dakusui.jcunit.compat.core.BasicSummarizer;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.Summarizer;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

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
      if (another == null) {
        return false;
      }
      if (!(another instanceof Example)) {
        return false;
      }
      return this.a == ((Example) another).a;
    }
  }

  @In
  public int    a;
  @In
  public int    b;
  @Out
  public int    c;
  @Out
  public Object obj;
  @Out
  public String str;
  @Out
  public Map<String, String> map = new HashMap<String, String>();

  @Rule
  public RuleSet rules = new JCUnitBase()
      .autoRuleSet(this).summarizer(
          summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Test
  public void test() {
    this.c = new Calc().calc(this.a, this.b);
    this.obj = new Example();
    ;
    this.str = "Hello";
    this.map.put("hi", "everyone!");
  }
}
