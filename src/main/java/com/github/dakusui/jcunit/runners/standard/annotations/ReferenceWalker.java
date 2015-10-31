package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

public class ReferenceWalker<T> {
  protected final TestClass testClass;
  protected final String    referrerName;

  public ReferenceWalker(TestClass testClass, String referrerName) {
    this.testClass = testClass;
    this.referrerName = referrerName;
  }

  public void walk(ReferenceHandler<T> handler, String[] terms) {
    for (String eachTerm : terms) {
      handler.handleTerm(this, eachTerm);
    }
  }

  public void walk(ReferenceHandler<T> builder, String term) {
    for (String each : term.replace(" ", "").split("&&")) {
      boolean negateOperator = each.startsWith("!");
      FrameworkMethod m;
      if (negateOperator) {
        each = each.substring(1);
      }
      m = FrameworkMethodUtils.getFrameworkMethodByName(testClass, each);
      builder.handleMethod(this, negateOperator, m);
    }
  }
}
