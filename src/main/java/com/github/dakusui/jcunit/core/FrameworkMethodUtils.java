package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FrameworkMethodUtils {
  /**
   * Validates a precondition method.
   * A precondition method is a method annotated with {@literal @}Precondition or referred to by annotation {@literal @}Given.
   * It is mainly used to determine if a test method (or methods) should be executed in advance.
   * <p/>
   * It must be public, static, returning a boolean value, and must have one and only parameter assignable from the test class.
   * In case it is not valid, this method add a string message which describes the failure to {@code failures} list.
   */
  private static void validatePreconditionMethod(Class<?> testClass, FrameworkMethod method, List<String> failures) {
    if (!method.isPublic()) {
      failures.add(String.format(
          "The method '%s' must be public. (in %s)", method.getName(), testClass.getCanonicalName()
      ));
    }
    if (!method.isStatic()) {
      failures.add(String.format(
          "The method '%s' must be static. (in %s)", method.getName(), testClass.getCanonicalName()
      ));
    }
    if (!Boolean.TYPE.equals(method.getReturnType())) {
      failures.add(String.format(
          "The method '%s' must return a boolean value, but '%s' is returned. (in %s)",
          method.getName(),
          method.getReturnType().getName(),
          testClass.getCanonicalName()
      ));
    }
    if (!(method.getMethod().getParameterTypes().length == 1 && method.getMethod().getParameterTypes()[0].isAssignableFrom(testClass))) {
      failures.add(String.format(
          "The method '%s' must take one and only one parameter, which is assignable from ' %s' (in %s)",
          method.getName(),
          method.getReturnType().getName(),
          testClass.getCanonicalName()
      ));
    }
  }


  public static interface FrameworkMethodValidator {
    /**
     * A validator used for custom test case methods.
     */
    public static final FrameworkMethodValidator CUSTOMTESTCASEMETHOD_VALIDATOR = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> failures) {
        Method mm = method.getMethod();
        if (!method.isPublic() && method.isStatic()
            && mm.getParameterTypes().length == 0 &&
            (Tuple.class.isAssignableFrom(mm.getReturnType()) ||
                (Iterable.class.isAssignableFrom(mm.getReturnType())
                ))) {
          failures.add("error");
        }
      }
    };

    /**
     * A validator used for methods referenced by a {@literal @}{@code Given} annotation.
     */
    public static final FrameworkMethodValidator VALIDATOR_FOR_METHOD_REFERENCEDBY_GIVEN = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> failures) {
        validatePreconditionMethod(testClass, method, failures);
      }
    };

    /**
     * A validator used for precondition methods.
     */
    public static final FrameworkMethodValidator PRECONDITIONMETHOD_VALIDATOR = new FrameworkMethodValidator() {
      @Override
      public void validate(Class<?> testClass, FrameworkMethod method, List<String> failures) {
        CompatFrameworkMethodUtils.validateTestPreconditionMethod(testClass, method, failures);
      }
    };

    /**
     * A method which validates a {@code FrameworkMethod}.
     */
    public void validate(Class<?> testClass, FrameworkMethod method, List<String> failures);
  }

  public interface FrameworkMethodRetriever {
    abstract static class FrameworkMethodRetrieverBase implements FrameworkMethodRetriever {
      @Override
      public List<FrameworkMethod> getMethods(TestClass testClass) {
        return testClass.getAnnotatedMethods(this.getAnnotation());
      }

      abstract protected Class<? extends Annotation> getAnnotation();
    }

    public static final FrameworkMethodRetriever CUSTOM_TESTCASES = new FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return CustomTestCases.class;
      }
    };

    /**
     * A retriever which gathers methods annotated with {@literal @}{@code Precondition}.
     */
    public static final FrameworkMethodRetriever PRECONDITION = new FrameworkMethodRetriever.FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return Precondition.class;
      }
    };

    /**
     * A retriever which gathers {@code FrameworkMethod}s referenced by {@literal @}{@code Given} annotation.
     */
    public static final FrameworkMethodRetriever REFERENCED_BY_GIVEN = new FrameworkMethodRetrieverBase() {
      @Override
      protected Class<? extends Annotation> getAnnotation() {
        return Given.class;
      }

      @Override
      public List<FrameworkMethod> getMethods(TestClass testClass) {
        List<FrameworkMethod> methodsAnnotatedWithGiven = super.getMethods(testClass);
        List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methodsAnnotatedWithGiven.size());
        for (FrameworkMethod each : methodsAnnotatedWithGiven) {
          Given ann = (Given) each.getMethod().getAnnotation(this.getAnnotation());
          ret.add(parse(testClass, ann));
        }
        return ret;
      }

      private CompositeFrameworkMethod parse(TestClass testClass, Given given) {
        CompositeFrameworkMethod.Builder b = new CompositeFrameworkMethod.Builder();
        b.setMode(CompositeFrameworkMethod.Mode.Or);
        for (String each : given.value()) {
          b.addMethod(false, parse(testClass, each));
        }
        return b.build();
      }

      private FrameworkMethod parse(TestClass testClass, String term) {
        CompositeFrameworkMethod.Builder b = new CompositeFrameworkMethod.Builder();
        b.setMode(CompositeFrameworkMethod.Mode.And);
        for (String each : term.replace(" ", "").split("&&")) {
          boolean negateOperator = each.startsWith("!");
          FrameworkMethod m;
          if (negateOperator) {
            m = new FrameworkMethod(CompatFrameworkMethodUtils.getTestPreconditionMethod(testClass.getJavaClass(), each.substring(1),  new LinkedList<String>()));
          } else {
            m = new FrameworkMethod(CompatFrameworkMethodUtils.getTestPreconditionMethod(testClass.getJavaClass(), each, new LinkedList<String>()));
          }
          b.addMethod(negateOperator, m);
        }
        return b.build();
      }
    };

    public List<FrameworkMethod> getMethods(TestClass testClass);
  }


  /**
   * A base class for JCUnit based FrameworkMethods.
   */
  static abstract class JCUnitFrameworkMethod extends FrameworkMethod {
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

    protected String getNameInSuper() {
      return super.getName();
    }
  }

  /**
   * This class represents a composite framework method (And and Or).
   */
  static class CompositeFrameworkMethod extends JCUnitFrameworkMethod {
    static {
      try {
        DUMMY_METHOD = CompositeFrameworkMethod.class.getMethod("dummyMethod");
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

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

    static enum Mode {
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


    public static final Method                DUMMY_METHOD;
    private final       Mode                  mode;
    private final       List<FrameworkMethod> methods;

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    CompositeFrameworkMethod(Mode mode, List<FrameworkMethod> methods) {
      super(DUMMY_METHOD);
      Utils.checknotnull(methods);
      Utils.checknotnull(mode, "Mode isn't set yet.");
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
      b.append("(");
      boolean firstTime = true;
      for (FrameworkMethod each : this.methods) {
        if (!firstTime) {
          b.append(this.mode);
        }
        b.append(each.getName());
        firstTime = false;
      }
      b.append(")");
      return b.toString();
    }

    @SuppressWarnings("unused")
    public static boolean dummyMethod() {
      return true;
    }
  }

  /**
   * Represents a JCUnit framework method which returns negated value of the original method.
   */
  static class NegatedFrameworkMethod extends JCUnitFrameworkMethod {
    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public NegatedFrameworkMethod(FrameworkMethod method) {
      super(method.getMethod()); // Use original method as a dummy.
    }

    @Override
    public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
      return !((Boolean) invokeExplosivelyInSuper(target, params));
    }

    @Override
    public String getName() {
      return String.format("!%s", getNameInSuper());
    }
  }
}
