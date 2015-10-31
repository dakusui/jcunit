package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.runners.standard.CompositeFrameworkMethod;
import org.junit.runners.model.FrameworkMethod;

import java.util.LinkedList;
import java.util.List;


public abstract class ReferenceHandler<T> {

  abstract public void handleMethod(ReferenceWalker<T> walker, boolean negateOperator, FrameworkMethod method);

  abstract public void handleTerm(ReferenceWalker<T> walker, String term);

  abstract public T handleTermArray(ReferenceWalker<T> walker, String[] terms);

  public static class ForBuildingCompositeFrameworkMethod extends ReferenceHandler<CompositeFrameworkMethod> {
    final CompositeFrameworkMethod.Builder builder;

    public ForBuildingCompositeFrameworkMethod() {
      this.builder = new CompositeFrameworkMethod.Builder();
      this.builder.setMode(CompositeFrameworkMethod.Mode.Or);
    }

    @Override
    public void handleMethod(ReferenceWalker<CompositeFrameworkMethod> walker, boolean negateOperator, FrameworkMethod method) {
      this.builder.addMethod(negateOperator, method);
    }

    @Override
    public void handleTerm(ReferenceWalker<CompositeFrameworkMethod> walker, String term) {
      ForBuildingCompositeFrameworkMethod b = new ForBuildingCompositeFrameworkMethod();
      b.builder.setMode(CompositeFrameworkMethod.Mode.And);
      walker.walk(b, term);
      this.builder.addMethod(false, b.builder.build());
    }

    @Override
    public CompositeFrameworkMethod handleTermArray(ReferenceWalker<CompositeFrameworkMethod> walker, String[] terms) {
      walker.walk(this, terms);
      return this.builder.build();
    }
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
