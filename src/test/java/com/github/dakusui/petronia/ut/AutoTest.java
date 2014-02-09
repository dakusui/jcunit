package com.github.dakusui.petronia.ut;

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
public class AutoTest extends DefaultRuleSetBuilder {
	@Rule
	public RuleSet rules = autoRuleSet(this).summarizer(summarizer);
	
	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();

	@In
	public int a;
	
	@Out
	public int test = 123;

	@Test
	public void test() {
		this.test--; 
	}
}
