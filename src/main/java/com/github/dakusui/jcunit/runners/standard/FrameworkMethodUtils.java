package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A class that holds utility methods to retrieve and validate framework methods.
 */
public class FrameworkMethodUtils {
  /**
   * Returns a {@code Method} object or {@code NotFoundMethod} if the specified method is not found or not loadable.
   */
  public static FrameworkMethod getFrameworkMethodByName(TestClass testClass, String methodName) {
    FrameworkMethod foundMethod = null;
    for (FrameworkMethod each : testClass.getAnnotatedMethods(Condition.class)) {
      if (methodName.equals(each.getName())) {
        if (foundMethod != null) {
          return new NotFoundMethod(methodName);
        }
        foundMethod = each;
      }
    }
    if (foundMethod == null) {
      return new NotFoundMethod(methodName);
    }
    return foundMethod;
  }

  public static void validateFrameworkMethod(Class<?> testClass, FrameworkMethod method, AnnotationValidator validator, List<Throwable> errors) {
    Checks.checknotnull(testClass);
    Checks.checknotnull(method);
    Checks.checknotnull(validator);
    Checks.checknotnull(errors);
    if (method instanceof CompositeFrameworkMethod) {
      for (FrameworkMethod each : ((CompositeFrameworkMethod) method).getChildren()) {
        validateFrameworkMethod(testClass, each, validator, errors);
      }
    } else if (method instanceof NotFoundMethod) {
      errors.add(new Exception(String.format("The method '%s' is not found or not unique in a class '%s'", method.getName(), testClass.getCanonicalName())));
    } else {
      errors.addAll(validator.validateAnnotatedMethod(method));
    }
  }

  public static CompositeFrameworkMethod buildCompositeFrameworkMethod(TestClass testClass, When from) {
    return new ReferenceHandler.ForBuildingCompositeFrameworkMethod()
        .handleTermArray(new ReferenceWalker<CompositeFrameworkMethod>(testClass, When.class.getAnnotation(ReferrerAttribute.class).value()), from.value());
  }

  public static List<FrameworkMethod> findReferencedFrameworkMethods(TestClass testClass, When by) {
    return new ReferenceHandler.ForCollectingReferencedMethods()
        .handleTermArray(new ReferenceWalker<List<FrameworkMethod>>(
                testClass,
                When.class.getAnnotation(ReferrerAttribute.class).value()),
            by.value());
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

  // TODO: Abolish this class
  public static class NotFoundMethod extends JCUnitFrameworkMethod {
    private final String methodName;

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public NotFoundMethod(String methodName) {
      super(DUMMY_METHOD);
      this.methodName = methodName;
    }

    @Override
    public Object invokeExplosively(Object target, Object... params) throws Throwable {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
      return this.methodName;
    }
  }

  public static String getPreconditionMethodNameFor(When ann) {
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
