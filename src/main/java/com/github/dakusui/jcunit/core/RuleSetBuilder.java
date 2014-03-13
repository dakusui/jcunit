package com.github.dakusui.jcunit.core;

import com.github.dakusui.lisj.Context;

public interface RuleSetBuilder extends Context {
  RuleSet ruleSet();

  RuleSet ruleSet(Object target);

  RuleSet autoRuleSet(Object obj, String... fields);
}
