package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;

import java.util.List;
import java.util.Map;

public class RegexTestSuiteBuilder extends RegexToFactorListTranslator {
  private final Context topLevelContext;

  public RegexTestSuiteBuilder(String prefix, Expr topLevelExpression) {
    super(prefix, topLevelExpression);
    this.topLevelContext = this.context;
  }

  public TestSuite buildTestSuite() {
    TestSuite.Builder builder = new TestSuite.Builder(new IpoGcCoveringArrayEngine(2));
    Factors factors = buildFactors();
    for (Factor eachFactor : factors) {
      builder.addFactor(eachFactor);
    }
    for (TestSuite.Predicate eachPredicate : buildConstraints(factors.asFactorList())) {
      System.out.println(eachPredicate);
      builder.addConstraint(eachPredicate);
    }
    return builder.build();
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("Factors:[\n");
    for (Map.Entry<String, List<Value>> each : this.terms.entrySet()) {
      b.append("  ");
      b.append(each.toString());
      b.append("\n");
    }
    b.append("],\n");
    b.append("TopLevel:[\n");
    b.append("  ");
    b.append(((Context.Impl) this.topLevelContext).seq.toString());
    b.append("\n");
    b.append("]");
    return b.toString();
  }

}
