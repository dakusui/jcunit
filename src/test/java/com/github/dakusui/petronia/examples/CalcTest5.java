package com.github.dakusui.petronia.examples;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class CalcTest5 {
	@In(domain = In.Domain.Method)
	public String[] f;

	public static String[][] f() {
		return new String[][] {
				new String[] { "hello", "world" }
		};
	}

	@Rule
	public RuleSet rules = new DefaultRuleSetBuilder().autoRuleSet(this).summarizer(summarizer);

	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();

	@Test
	public void test() {
	}
}
