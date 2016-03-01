package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.runners.standard.CompositeFrameworkMethod;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.List;


public abstract class ReferenceHandler<T> {

  abstract public void handleMethod(ReferenceWalker<T> walker, boolean negateOperator, FrameworkMethod method);

  abstract public void handleTerm(ReferenceWalker<T> walker, String term);

  abstract public T handleTermArray(ReferenceWalker<T> walker, String[] terms);

  public static class CompositeFrameworkMethodBuilderForReferenceHandler extends ReferenceHandler<CompositeFrameworkMethod> {
    final CompositeFrameworkMethod.Builder builder;

    public CompositeFrameworkMethodBuilderForReferenceHandler(CompositeFrameworkMethod.Mode mode) {
      this.builder = new CompositeFrameworkMethod.Builder(mode);
    }

    @Override
    public void handleMethod(ReferenceWalker<CompositeFrameworkMethod> walker, boolean negateOperator, FrameworkMethod method) {
      this.builder.addMethod(negateOperator, method);
    }

    @Override
    public void handleTerm(ReferenceWalker<CompositeFrameworkMethod> walker, String term) {
      CompositeFrameworkMethodBuilderForReferenceHandler b = new CompositeFrameworkMethodBuilderForReferenceHandler(CompositeFrameworkMethod.Mode.And);
      walker.walk(b, term);
      this.builder.addMethod(false, b.builder.build());
    }

    @Override
    public CompositeFrameworkMethod handleTermArray(ReferenceWalker<CompositeFrameworkMethod> walker, String[] terms) {
      walker.walk(this, terms);
      return this.builder.build();
    }
  }

  static class FrameworkMethodValidationHandler extends ReferenceHandler<List<Exception>> {
    private final List<Exception> errors;
    private final String          referrerName;
    private final TestClass       testClass;

    FrameworkMethodValidationHandler(TestClass testClass, String referrerName, List<Exception> errors) {
      this.testClass = testClass;
      this.referrerName = referrerName;
      this.errors = errors;
    }

    @Override
    public void handleMethod(ReferenceWalker<List<Exception>> walker, boolean negateOperator, FrameworkMethod method) {
      ////
      // Validation specific logic follows
      if (method.getAnnotation(Condition.class) == null) {
        errors.add(new Exception(
            StringUtils.format(
                "Method '%s' referenced by '%s' in '%s' was found in the class but not annotated with @%s",
                method.getName(),
                referrerName,
                testClass.getName(),
                Condition.class
            )));
      }
    }

    @Override
    public void handleTerm(ReferenceWalker<List<Exception>> walker, String term) {
      walker.walk(this, term);
    }

    @Override
    public List<Exception> handleTermArray(ReferenceWalker<List<Exception>> walker, String[] terms) {
      walker.walk(this, terms);
      return this.errors;
    }
  }
}
