package com.github.dakusui.jcunit.core;

import org.junit.rules.TestRule;

public interface Summarizer extends TestRule {

	void passed(String testName, int id);

	void failed(String testName, int id);

	void setRuleSet(RuleSet ruleSet);

	int passes(int objId);

	int fails(int objId);
}
