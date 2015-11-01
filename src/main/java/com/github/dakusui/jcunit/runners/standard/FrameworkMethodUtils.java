package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A class that holds utility methods to retrieve and validate framework methods.
 */
public class FrameworkMethodUtils {
  public static CompositeFrameworkMethod buildCompositeFrameworkMethod(TestClass testClass, When from) {
    return new ReferenceHandler.ForBuildingCompositeFrameworkMethod()
        .handleTermArray(new ReferenceWalker<CompositeFrameworkMethod>(testClass, When.class.getAnnotation(ReferrerAttribute.class).value()), from.value());
  }

  public interface FrameworkMethodRetriever {
    abstract class FrameworkMethodRetrieverBase implements FrameworkMethodRetriever {
      @Override
      public List<FrameworkMethod> getMethods(Class<?> testClass) {
        return new TestClass(testClass).getAnnotatedMethods(this.getAnnotation());
      }

      abstract protected Class<? extends Annotation> getAnnotation();
    }

    FrameworkMethodRetriever CUSTOM_TESTCASES = new FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return CustomTestCases.class;
      }
    };

    /**
     * A retriever which gathers methods annotated with {@literal @}{@code Precondition}.
     */
    FrameworkMethodRetriever PRECONDITION = new FrameworkMethodRetriever.FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return Precondition.class;
      }
    };

    List<FrameworkMethod> getMethods(Class<?> testClass);
  }


  /**
   * A base class for JCUnit specific FrameworkMethods.
   */
  static abstract class JCUnitFrameworkMethod extends FrameworkMethod {
    public static final Method DUMMY_METHOD;

    static {
      try {
        DUMMY_METHOD = CompositeFrameworkMethod.class.getMethod("dummyMethod");
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public JCUnitFrameworkMethod(Method method) {
      super(method);
    }

    @Override
    public abstract Object invokeExplosively(final Object target, final Object... params) throws Throwable;

    @Override
    public abstract String getName();

    protected Object invokeExplosivelyInSuper(final Object target, final Object... params) throws Throwable {
      return super.invokeExplosively(target, params);
    }
  }

  /**
   * Represents a JCUnit framework method which returns negated value of the original method.
   */
  static class NegatedFrameworkMethod extends JCUnitFrameworkMethod {
    private final FrameworkMethod enclosedMethod;

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public NegatedFrameworkMethod(FrameworkMethod method) {
      super(method.getMethod()); // Use original method.
      this.enclosedMethod = method;
    }

    @Override
    public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
      return !((Boolean) invokeExplosivelyInSuper(target, params));
    }

    @Override
    public String getName() {
      return String.format("!%s", this.enclosedMethod.getName());
    }
  }

  public static String composePreconditionCompositeFrameworkMethodName(When ann) {
    Checks.checknotnull(ann);
    StringBuilder b = new StringBuilder();
    String[] values = ann.value();
    if (values.length > 1) {
      b.append('(');
    }
    boolean firstTime = true;
    for (String each : values) {
      if (!firstTime) {
        b.append("||");
      }
      b.append(normalize(each));
      firstTime = false;
    }
    if (values.length > 1) {
      b.append(')');
    }
    return b.toString();
  }

  private static String normalize(String term) {
    StringBuilder b = new StringBuilder();
    String[] tokens = term.replace(" ", "").split("&&");
    if (tokens.length > 1)
      b.append('(');
    boolean firstTime = true;
    for (String each : tokens) {
      if (!firstTime)
        b.append("&&");
      b.append(each);
      firstTime = false;
    }
    if (tokens.length > 1)
      b.append(')');
    return b.toString();
  }
}
