package com.github.dakusui.jcunit.core;

import org.junit.rules.TestRule;

public interface Summarizer extends TestRule {

  void passed(String testName, int id);

  void failed(String testName, int id);

  void setRuleSet(RuleSet ruleSet);

  int passes(int objId);

  int fails(int objId);

  void error(String methodName);

  void ok(String methodName);

  void ng(String methodName);

  void error(String string, int idOf);
}
