package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class FrameworkMethodUtils {
    private static boolean validateTestPreconditionMethod(Class<?> testClass, FrameworkMethod method, List<String> failures) {
        boolean ret = true;
        if (!method.isPublic()) {
            failures.add(String.format(
                    "The method '%s' must be public. (in %s)", method.getName(), testClass.getCanonicalName()
            ));
            ret = false;
        }
        if (!method.isStatic()) {
            failures.add(String.format(
                    "The method '%s' must be static. (in %s)", method.getName(), testClass.getCanonicalName()
            ));
            ret = false;
        }
        if (!Boolean.TYPE.equals(method.getReturnType())) {
            failures.add(String.format(
                    "The method '%s' must return a boolean value, but '%s' is returned. (in %s)",
                    method.getName(),
                    method.getReturnType().getName(),
                    testClass.getCanonicalName()
            ));
            ret = false;
        }
        if (!(method.getMethod().getParameterTypes().length == 1 && method.getMethod().getParameterTypes()[0].isAssignableFrom(testClass))) {
            failures.add(String.format(
                    "The method '%s' must take one and only one parameter, which is assignable from ' %s' (in %s)",
                    method.getName(),
                    method.getReturnType().getName(),
                    testClass.getCanonicalName()
            ));
            ret = false;
        }
        return ret;
    }

    /**
     * Returns {@code null}, if the {@code testMethod} doesn't have 'When' annotation,
     * which means the method should be executed without any preconditions.
     */
    static List<FrameworkMethod> getTestPreconditionMethodsFor(Class<?> testClass,
                                                               FrameworkMethod testMethod, List<String> failures) {
        List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
        Given given = testMethod.getAnnotation(Given.class);

        if (given == null) {
            return null;
        }

        for (String methodName : given.value()) {
            FrameworkMethod m = getTestPreconditionMethod(
                    testClass,
                    methodName,
                    testMethod,
                    failures);
            if (m != null) {
                ret.add(m);
            }
        }

        for (FrameworkMethod each : ret) {
            FrameworkMethodUtils.validateTestPreconditionMethod(testClass, each, failures);
        }
        return ret;
    }

    /**
     * Returns a {@code Method} object or {@code null} if the specified method is not found or not loadable.
     */
    private static FrameworkMethod getTestPreconditionMethod(Class<?> testClass,
                                                             String methodName, FrameworkMethod referredToBy, List<String> failures) {
        final List<FrameworkMethod> work = new LinkedList<FrameworkMethod>();
        for (String each : methodName.replace(" ", "").split("&&")) {
            work.add(getSingleTestPreconditionMethod(testClass, each, referredToBy,
                    failures));
        }
        Utils.checkcond(!work.isEmpty());
        if (work.size() == 1) {
            return work.get(0);
        }
        ////
        // In order to avoid NPE, pass the first element's underlying 'method' object.
        // But this should never be used directly.
        return new FrameworkMethod(work.get(0).getMethod()) {
            @Override
            public Object invokeExplosively(final Object target,
                                            final Object... params) throws Throwable {
                ////
                // It's safe to cast to Boolean because m is already validated by 'getTestPreconditionMethod'
                for (FrameworkMethod each : work) {
                    if (!(Boolean) each.invokeExplosively(target, params)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public String getName() {
                StringBuilder b = new StringBuilder();
                boolean firstTime = true;
                for (FrameworkMethod each : work) {
                    if (!firstTime) {
                        b.append("&&");
                    }
                    b.append(each.getName());
                    firstTime = false;
                }
                return b.toString();
            }

            @Override
            public Method getMethod() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static FrameworkMethod getSingleTestPreconditionMethod(
            Class<?> testClass, String methodName, FrameworkMethod referredToBy,
            List<String> failures) {
        boolean negateOperator = methodName.startsWith("!");
        methodName = negateOperator ? methodName.substring(1) : methodName;
        Method m = getTestPreconditionMethod(testClass, methodName, failures);
        if (m == null) {
            failures.remove(failures.size() - 1);
            ////
            // Funky thing: reformat the last message.
            failures.add(String.format(
                    "The method '%s(%s)' (referred to by '%s' of method '%s') can't be found in the test class '%s' .",
                    methodName,
                    testClass,
                    Given.class.getSimpleName(),
                    referredToBy.getName(),
                    testClass.getName()
            ));
            return null;
        }
        return negateOperator ? new FrameworkMethod(m) {
            @Override
            public Object invokeExplosively(final Object target,
                                            final Object... params) throws Throwable {
                ////
                // It's safe to cast to Boolean because m is already validated by 'getTestPreconditionMethod'
                return !((Boolean) super.invokeExplosively(target, params));
            }

            @Override
            public String getName() {
                return "!" + super.getName();
            }
        } : new FrameworkMethod(m);
    }

    /**
     * Returns a {@code Method} object or {@code null} if the specified method is not found or not loadable.
     */
    static Method getTestPreconditionMethod(Class<?> testClass, String methodName, List<String> failures) {
        try {
            return testClass.getMethod(methodName, testClass);
        } catch (NoSuchMethodException e) {
            failures.add(String.format(
                    "The method '%s(%s)' can't be found in the test class '%s'.",
                    methodName,
                    testClass,
                    testClass.getName()
            ));
            return null;
        }
    }

    public interface FrameworkMethodValidator {
      public static final FrameworkMethodValidator CUSTOM_TESTCASES = new FrameworkMethodValidatorBase() {
        @Override
        public boolean validate(Class<?> testClass, FrameworkMethod m, List<String> failures) {
          Method mm = m.getMethod();
          return m.isPublic() && m.isStatic()
              && mm.getParameterTypes().length == 0 &&
              (Tuple.class.isAssignableFrom(mm.getReturnType()) ||
                  (Iterable.class.isAssignableFrom(mm.getReturnType())
                  ));
        }

        @Override
        protected Class<? extends Annotation> getAnnotation() {
          return CustomTestCases.class;
        }
      };

      public static final FrameworkMethodValidator PRECONDITION = new FrameworkMethodValidatorBase() {
        @Override
        public boolean validate(Class<?> testClass, FrameworkMethod m, List<String> failures) {
          return validateTestPreconditionMethod(testClass, m, failures);
        }

        @Override
        protected Class<? extends Annotation> getAnnotation() {
          return Precondition.class;
        }
      };

      public static final FrameworkMethodValidator REFERENCED_BY_GIVEN = new FrameworkMethodValidatorBase() {
        class MethodRef {
          boolean negate;
          String methodName;
        }
        @Override
        protected Class<? extends Annotation> getAnnotation() {
          return Given.class;
        }

        @Override
        public boolean validate(Class<?> testClass, FrameworkMethod m, List<String> failures) {
          return validateTestPreconditionMethod(testClass, m, failures);
        }

        @Override
        public List<FrameworkMethod> getMethods(TestClass testClass) {
          List<FrameworkMethod> methodsAnnotatedWithGiven = super.getMethods(testClass);
          List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methodsAnnotatedWithGiven.size());
          for (FrameworkMethod each : methodsAnnotatedWithGiven) {
            Given ann = (Given) each.getMethod().getAnnotation(this.getAnnotation());
            String[] terms = ann.value();
          }
          return ret;
        }
      };

      public boolean validate(Class<?> testClass, FrameworkMethod m, List<String> failures);

      public List<FrameworkMethod> getMethods(TestClass testClass);

      abstract static class FrameworkMethodValidatorBase implements FrameworkMethodValidator {
        public List<FrameworkMethod> getMethods(TestClass testClass) {
          return testClass.getAnnotatedMethods(this.getAnnotation());
        }

        abstract protected Class<? extends Annotation> getAnnotation();
      }
    }

  static class CompositeFrameworkMethod extends FrameworkMethod {
    public static final Method DUMMY_METHOD;

    static {
      try {
        DUMMY_METHOD = CompositeFrameworkMethod.class.getMethod("dummyMethod");
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    static class Builder {
      public Builder addMethod(Method method) {
        return this;
      }
    }

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public CompositeFrameworkMethod() {
      super(DUMMY_METHOD);
    }

    @SuppressWarnings("unused")
    public static void dummyMethod() {
    }
  }
}
