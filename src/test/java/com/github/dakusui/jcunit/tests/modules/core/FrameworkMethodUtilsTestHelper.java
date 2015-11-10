package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.runners.standard.annotations.ReferenceHandler;
import com.github.dakusui.jcunit.runners.standard.annotations.ReferenceWalker;
import com.github.dakusui.jcunit.runners.standard.annotations.ReferrerAttribute;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.LinkedList;
import java.util.List;

public class FrameworkMethodUtilsTestHelper {
  public static List<FrameworkMethod> findReferencedFrameworkMethods(TestClass testClass, When by) {
    return new ForCollectingReferencedMethods()
        .handleTermArray(new ReferenceWalker<List<FrameworkMethod>>(
                testClass,
                When.class.getAnnotation(ReferrerAttribute.class).value()),
            by.value());
  }

  public static class ForCollectingReferencedMethods extends ReferenceHandler<List<FrameworkMethod>> {

    final private List<FrameworkMethod> frameworkMethods;

    public ForCollectingReferencedMethods() {
      this.frameworkMethods = new LinkedList<FrameworkMethod>();
    }

    @Override
    public void handleMethod(ReferenceWalker<List<FrameworkMethod>> walker, boolean negateOperator, FrameworkMethod method) {
      this.frameworkMethods.add(method);
    }

    @Override
    public void handleTerm(ReferenceWalker<List<FrameworkMethod>> walker, String term) {
      walker.walk(this, term);
    }

    @Override
    public List<FrameworkMethod> handleTermArray(ReferenceWalker<List<FrameworkMethod>> walker, String[] terms) {
      walker.walk(this, terms);
      return this.frameworkMethods;
    }
  }
}
