package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
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

  /**
   * Returns a {@code Method} object or {@code NotFoundMethod} if the specified method is not found or not loadable.
   */
  public static FrameworkMethod getFrameworkMethodByName(TestClass testClass, String methodName) {
    FrameworkMethod foundMethod = null;
    for (FrameworkMethod each : testClass.getAnnotatedMethods(Condition.class)) {
      if (methodName.equals(each.getName())) {
        Checks.checktest(
            foundMethod == null,
            "The method '%s' is not unique in class '%s'", methodName, testClass.getJavaClass()
        );
        foundMethod = each;
      }
    }
    Checks.checktest(
        foundMethod != null,
        "The method '%s' is not found in class '%s'", methodName, testClass.getJavaClass()
    );
    return foundMethod;
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
      m = getFrameworkMethodByName(testClass, each);
      builder.handleMethod(this, negateOperator, m);
    }
  }
}
