package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.List;

public class ReferenceWalker<T> {
  protected final TestClass testClass;
  protected final String    referrerName;
  private final List<FrameworkMethod> conditonMethods;

  public ReferenceWalker(TestClass testClass, String referrerName) {
    this.testClass = testClass;
    this.referrerName = referrerName;
    this.conditonMethods = this.getConditionMethods(this.testClass);
  }

  /**
   * Returns a {@code Method} object or {@code NotFoundMethod} if the specified method is not found or not loadable.
   */
  private FrameworkMethod getFrameworkMethodByName(String methodName) {
    FrameworkMethod foundMethod = null;
    for (FrameworkMethod each : conditonMethods) {
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

  private static List<FrameworkMethod> getConditionMethods(TestClass testClass) {
    // TODO: Issue-#49 Enhance this method to return methods that invoked specified constraints by user.
    return testClass.getAnnotatedMethods(Condition.class);
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
      m = getFrameworkMethodByName(each);
      builder.handleMethod(this, negateOperator, m);
    }
  }
}
