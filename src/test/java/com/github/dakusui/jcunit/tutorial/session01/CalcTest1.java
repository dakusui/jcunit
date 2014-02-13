package com.github.dakusui.jcunit.tutorial.session01;

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
public class CalcTest1 {
	@In
	public int a;
	@In
	public int b;
	@Out
	public int c;
	
	@Rule
	public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this).summarizer(summarizer);
	
	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();
	
	@Test
	public void test() {
		this.c = new Calc().calc(this.a, this.b);
	}
}
