package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitUserException;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    try {
      ////
      // Prepare filter method(s) and custom test case methods.
      List<String> frameworkMethodFailures = new LinkedList<String>();
      List<FrameworkMethod> preconditionMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodValidator.PRECONDITION);
      // Currently only one filter method can be used.
      // Custom test case methods.
      List<FrameworkMethod> customTestCaseMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodValidator.CUSTOM_TESTCASES);
      ////
      // Check if any error was found.
      ConfigUtils.checkEnv(frameworkMethodFailures.isEmpty(),
          "Errors are found in test class '%s':%s",
          getTestClass().getJavaClass().getCanonicalName(),
          frameworkMethodFailures);

      ////
      // Generate a list of test cases using a specified tuple generator
      TupleGenerator tupleGenerator = TupleGeneratorFactory.INSTANCE
          .createTupleGeneratorFromClass(klass);
      Factors factors = tupleGenerator.getFactors();
      int id;
      for (id = (int) tupleGenerator.firstId();
           id >= 0; id = (int) tupleGenerator.nextId(id)) {
        Tuple testCase = tupleGenerator.get(id);
        if (shouldPerform(testCase, preconditionMethods)) {
          runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
              id, TestCaseType.Generated,
              factors,
              testCase));
        }
      }
      // Skip to number of test cases generated.
      id = (int) tupleGenerator.size();
      ////
      // Compose a list of 'negative test cases' and register them.
      ConstraintManager cm = tupleGenerator.getConstraintManager();
      final List<Tuple> violations = cm.getViolations();
      id = registerTestCases(
          id,
          factors,
          violations,
          TestCaseType.Violation,
          preconditionMethods);
      ////
      // Compose a list of 'custom test cases' and register them.
      registerTestCases(
          id,
          factors,
          invokeCustomTestCasesMethod(customTestCaseMethods),
          TestCaseType.Custom,
          preconditionMethods);
      ConfigUtils.checkEnv(runners.size() > 0, "No test to be run was found.");
    } catch (JCUnitUserException e) {
      e.setTargetClass(klass);
      throw e;
    }
  }

  static Object createTest(TestClass testClass, Tuple testCase) {
    return TestCaseUtils.toTestObject(testClass.getJavaClass(), testCase);
  }

  private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
    if (preconditionMethods.isEmpty()) return true;
    for (FrameworkMethod m : preconditionMethods) {
      try {
        Object testObject = createTest(this.getTestClass(), testCase);
        if ((Boolean)m.invokeExplosively(null, testObject)) return true;
      } catch (Throwable throwable) {
        ConfigUtils.rethrow(throwable, "Failed to execute ");
      }
    }
    return false;
  }

  private int registerTestCases(int id,
      Factors factors,
      Iterable<Tuple> testCases,
      TestCaseType testCaseType,
      List<FrameworkMethod> preconditionMethods)
      throws Throwable {
    for (Tuple testCase : testCases) {
      if (shouldPerform(testCase, preconditionMethods)) {
        runners.add(new JCUnitRunner(
            getTestClass().getJavaClass(),
            id,
            testCaseType,
            factors,
            testCase));
      }
      id++;
    }
    return id;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<Tuple> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
    List<Tuple> ret = new LinkedList<Tuple>();
    try {
      for (FrameworkMethod m : customTestCasesMethods) {
        Object r = m.invokeExplosively(null);

        if (r instanceof Tuple) {
          ret.add((Tuple) r);
        } else if (r instanceof Iterable) {
          for (Object o : (Iterable) r) {
            if (o == null) {
              ConfigUtils.checkEnv(false, "Returned value of '%s' must not contain null.", m.getName());
            }
            if (o instanceof Tuple) {
              ret.add((Tuple) o);
            } else if (getTestClass().getJavaClass().isAssignableFrom(o.getClass())) {
              ret.add(TestCaseUtils.toTestCase(o));
            } else {
              ConfigUtils.checkEnv(false, "Returned value of '%s' must contain only Tuple or test objects.", m.getName());
            }
          }
        } else {
          Utils.checkcond(false);
        }
      }
    } catch (Throwable throwable) {
      Utils.rethrow(throwable, "Failed to execute '%s'.: (%s)", throwable.getMessage());
    }
    return ret;
  }

  private List<FrameworkMethod> getFrameworkMethods(List<String> failures, FrameworkMethodValidator validator) {
    Class<? extends Annotation> annClass = validator.getAnnotation();
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annClass);
    List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methods.size());
    for (FrameworkMethod m : methods) {
      if (validator.validate(getTestClass().getJavaClass(), m)) {
        ret.add(m);
      } else {
        failures.add(
            String.format(
                "Method '%s'(annotated by '%s') must satisfy the following conditions: %s",
                m.getName(),
                annClass.getName(),
                validator.getDescription()
            ));
      }
    }
    return ret;
  }

  static void validateTestPreconditionMethod(Class<?> testClass, FrameworkMethod method, List<String> failures) {
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
    if (Boolean.class.equals(method.getReturnType()) && method.getReturnType().isPrimitive()) {
      failures.add(String.format(
          "The method '%s' must return a boolean value. (in %s)", method.getName(), testClass.getCanonicalName()
      ));
    }
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
    public static final FrameworkMethodValidator CUSTOM_TESTCASES = new FrameworkMethodValidator() {
      @Override
      public boolean validate(Class testClass, FrameworkMethod m) {
        Method mm = m.getMethod();
        return m.isPublic() && m.isStatic() && mm.getParameterTypes().length == 0 &&
            (Tuple.class.isAssignableFrom(mm.getReturnType()) ||
                (Iterable.class.isAssignableFrom(mm.getReturnType())
                ));
      }

      @Override
      public Class<? extends Annotation> getAnnotation() {
        return CustomTestCases.class;
      }

      @Override
      public String getDescription() {
        return "public, static, no parameter, and returning 'LabeledTestCase' or an iterable of it";
      }
    };

    public static final FrameworkMethodValidator PRECONDITION = new FrameworkMethodValidator() {
      @Override
      public boolean validate(Class<?> testClass, FrameworkMethod m) {
        Method mm = m.getMethod();
        boolean ret;
        ret = mm.getParameterTypes().length == 1;
        ret &= mm.getParameterTypes()[0].isAssignableFrom(testClass);
        List<String> failures = new LinkedList<String>();
        validateTestPreconditionMethod(m.getDeclaringClass(), m, failures);
        ret &= failures.isEmpty();
        return ret;

      }

      @Override
      public Class<? extends Annotation> getAnnotation() {
        return Precondition.class;
      }

      @Override
      public String getDescription() {
        return "public, static, returning boolean, accepting an object of the class in which it is declared as the first parameter";
      }
    };

    public boolean validate(Class<?> testClass, FrameworkMethod m);

    public Class<? extends Annotation> getAnnotation();

    public String getDescription();
  }

  public static class TestCaseInternalAnnotation implements Annotation {

    private final TestCaseType type;
    private final int          id;
    private       Factors      factors;
    private       Tuple        testCase;

    public TestCaseInternalAnnotation(TestCaseType type, int id, Factors factors, Tuple testCase) {
      Utils.checknotnull(type);
      this.id = id;
      this.type = type;
      this.factors = factors;
      this.testCase = testCase;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return this.getClass();
    }

    public int getId() {
      return this.id;
    }

    public TestCaseType getTestCaseType() {
      return this.type;
    }

    public Tuple getTestCase() {
      return testCase;
    }

    public Factors getFactors() {
      return factors;
    }
  }

  public static enum TestCaseType {
    Custom,
    Generated,
    Violation
  }
}
