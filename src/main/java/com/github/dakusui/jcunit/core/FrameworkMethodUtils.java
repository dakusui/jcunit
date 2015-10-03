package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that holds utility methods to retrieve and validate framework methods.
 */
public class FrameworkMethodUtils {
  /**
   * Returns a {@code Method} object or {@code null} if the specified method is not found or not loadable.
   */
  private static FrameworkMethod getFrameworkMethodByName(Class<?> testClass, String methodName) {
    Method foundMethod = null;
    for (Method m : testClass.getDeclaredMethods()) {
      if (methodName.equals(m.getName())) {
        if (foundMethod != null) {
          return new NotFoundMethod(methodName);
        }
        foundMethod = m;
      }
    }
    if (foundMethod == null) {
      return new NotFoundMethod(methodName);
    }
    return new FrameworkMethod(foundMethod);
  }

  /**
   * Validates a precondition method.
   * A precondition method is a method annotated with {@literal @}Precondition or referred to by an annotation {@literal @}Given.
   * It is mainly used to determine if a test method (or methods) should be executed in advance.
   * <p/>
   * It must be public, non-static, returning a boolean value, and must have no parameter.
   * In case it is not valid, this method add a string message which describes the failure to {@code errors} list.
   */
  private static void validatePreconditionFrameworkMethod(Class<?> testClass, FrameworkMethod method, List<String> errors) {
    if (!method.isPublic()) {
      errors.add(String.format(
          "The method '%s' must be public. (in %s)", method.getName(), testClass.getCanonicalName()
      ));
    }
    if (method.isStatic()) {
      errors.add(String.format(
          "The method '%s' must not be static. (in %s)", method.getName(), testClass.getCanonicalName()
      ));
    }
    if (!Boolean.TYPE.equals(method.getReturnType())) {
      errors.add(String.format(
          "The method '%s' must return a boolean value, but '%s' is returned. (in %s)",
          method.getName(),
          method.getReturnType().getName(),
          testClass.getCanonicalName()
      ));
    }
    Class<?>[] parameterTypes = method.getMethod().getParameterTypes();
    if (parameterTypes.length != 0) {
      errors.add(String.format(
          "The method '%s' must not have any parameter. (in %s)",
          method.getName(),
          testClass.getCanonicalName()
      ));
    }
  }

  public static void validateFrameworkMethod(Class<?> testClass, FrameworkMethod method, FrameworkMethodValidator validator, List<String> errors) {
    Checks.checknotnull(testClass);
    Checks.checknotnull(method);
    Checks.checknotnull(validator);
    Checks.checknotnull(errors);
    if (method instanceof CompositeFrameworkMethod) {
      for (FrameworkMethod each : ((CompositeFrameworkMethod) method).methods) {
        validateFrameworkMethod(testClass, each, validator, errors);
      }
    } else if (method instanceof NotFoundMethod) {
      errors.add(String.format("The method '%s' is not found or not unique in a class '%s'", method.getName(), testClass.getCanonicalName()));
    } else {
      validator.validate(testClass, method, errors);
    }
  }


  public interface FrameworkMethodValidator {
    /**
     * A validator used for custom test case methods.
     */
    FrameworkMethodValidator CUSTOMTESTCASEMETHOD_VALIDATOR = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> errors) {
        Method mm = method.getMethod();
        if (!method.isPublic() && method.isStatic()
            && mm.getParameterTypes().length == 0 &&
            (Tuple.class.isAssignableFrom(mm.getReturnType()) ||
                (Iterable.class.isAssignableFrom(mm.getReturnType())
                ))) {
          errors.add("error");
        }
      }
    };

    /**
     * A validator used for methods referenced by a {@literal @}{@code Given} annotation.
     */
    FrameworkMethodValidator VALIDATOR_FOR_METHOD_REFERENCEDBY_WHEN = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> errors) {
        validatePreconditionFrameworkMethod(testClass, method, errors);
      }
    };

    /**
     * A validator used for precondition methods.
     */
    FrameworkMethodValidator PRECONDITIONMETHOD_VALIDATOR = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> errors) {
        validatePreconditionFrameworkMethod(testClass, method, errors);
      }
    };

    /**
     * A method which validates a {@code FrameworkMethod}.
     */
    void validate(Class<?> testClass, FrameworkMethod method, List<String> errors);
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

    /**
     * A retriever which gathers {@code FrameworkMethod}s referenced by {@literal @}{@code Given} annotation.
     */
    FrameworkMethodRetriever REFERENCED_BY_WHEN = new FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return When.class;
      }

      @Override
      public List<FrameworkMethod> getMethods(Class<?> testClass) {
        List<FrameworkMethod> methodsAnnotatedWithGiven = super.getMethods(testClass);
        List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methodsAnnotatedWithGiven.size());
        for (FrameworkMethod each : methodsAnnotatedWithGiven) {
          When ann = (When) each.getMethod().getAnnotation(this.getAnnotation());
          ret.add(parse(testClass, ann));
        }
        return ret;
      }

      private CompositeFrameworkMethod parse(Class<?> testClass, When when) {
        CompositeFrameworkMethod.Builder b = new CompositeFrameworkMethod.Builder();
        b.setMode(CompositeFrameworkMethod.Mode.Or);
        for (String each : when.value()) {
          b.addMethod(false, parse(testClass, each));
        }
        return b.build();
      }

      private FrameworkMethod parse(Class<?> testClass, String term) {
        CompositeFrameworkMethod.Builder b = new CompositeFrameworkMethod.Builder();
        b.setMode(CompositeFrameworkMethod.Mode.And);
        for (String each : term.replace(" ", "").split("&&")) {
          boolean negateOperator = each.startsWith("!");
          FrameworkMethod m;
          if (negateOperator) {
            m = getFrameworkMethodByName(testClass, each.substring(1));
          } else {
            m = getFrameworkMethodByName(testClass, each);
          }
          b.addMethod(negateOperator, m);
        }
        return b.build();
      }
    };

    List<FrameworkMethod> getMethods(Class<?> testClass);
  }


  /**
   * A base class for JCUnit based FrameworkMethods.
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

  /**
   * This class represents a composite framework method (And and Or).
   */
  static class CompositeFrameworkMethod extends JCUnitFrameworkMethod {
    static class Builder {
      private List<FrameworkMethod> methods = new LinkedList<FrameworkMethod>();
      Mode mode = null;

      public Builder() {
      }

      public Builder setMode(Mode mode) {
        this.mode = mode;
        return this;
      }

      public Builder addMethod(boolean negate, FrameworkMethod method) {
        FrameworkMethod m;
        if (negate) {
          m = new NegatedFrameworkMethod(method);
        } else {
          m = method;
        }
        methods.add(m);
        return this;
      }

      public CompositeFrameworkMethod build() {
        return new CompositeFrameworkMethod(this.mode, this.methods);
      }
    }

    enum Mode {
      And {
        public String toString() {
          return "&&";
        }
      },
      Or {
        public String toString() {
          return "||";
        }
      }
    }

    private final Mode                  mode;
    private final List<FrameworkMethod> methods;

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    CompositeFrameworkMethod(Mode mode, List<FrameworkMethod> methods) {
      super(DUMMY_METHOD);
      Checks.checknotnull(methods);
      Checks.checknotnull(mode, "Mode isn't set yet.");
      this.methods = methods;
      this.mode = mode;
    }

    @Override
    public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
      if (mode == Mode.And) {
        boolean ret = true;
        for (FrameworkMethod each : this.methods) {
          ret &= (Boolean) each.invokeExplosively(target, params);
        }
        return ret;
      } else if (mode == Mode.Or) {
        boolean ret = false;
        for (FrameworkMethod each : this.methods) {
          FSMUtils.resetStories(target);
          ret |= (Boolean) each.invokeExplosively(target, params);
        }
        return ret;
      }
      assert false;
      return null;
    }

    @Override
    public String getName() {
      StringBuilder b = new StringBuilder();
      if (this.methods.size() > 1) {
        b.append("(");
      }
      boolean firstTime = true;
      for (FrameworkMethod each : this.methods) {
        if (!firstTime) {
          b.append(this.mode);
        }
        b.append(each.getName());
        firstTime = false;
      }
      if (this.methods.size() > 1) {
        b.append(")");
      }
      return b.toString();
    }

    @SuppressWarnings("unused")
    public static boolean dummyMethod() {
      return true;
    }
  }

  static class NotFoundMethod extends JCUnitFrameworkMethod {
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
