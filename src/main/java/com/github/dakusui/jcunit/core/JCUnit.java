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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <pre>
 * # | '@Test' | '@Then' |
 * 1 | Absent  | Absent  | The method will not be executed.
 * 2 | Absent  | Present | The method will be executed when a method whose name appears in '@Then' 's value returns true.
 * 3 | Present | Absent  | The method will be executed always. Parameters given to @Test will be respected.
 * 4 | Present | Present | The method will be executed same as #2. Parameters given to @Test will also be respected.
 * </pre>
 */
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
      ConfigUtils.checkEnv(frameworkMethodFailures.isEmpty(), "Errors are found in test class '%s':%s", getTestClass().getJavaClass().getCanonicalName(), frameworkMethodFailures);

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
      final List<LabeledTestCase> violations = cm.getViolations();
      id = registerLabeledTestCases(
          id,
          factors,
          violations,
          TestCaseType.Violation,
          preconditionMethods);
      ////
      // Compose a list of 'custom test cases' and register them.
      registerLabeledTestCases(
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

  private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
    boolean ret = true;
    for (FrameworkMethod m : preconditionMethods) {
      try {
        ret &= (Boolean)m.invokeExplosively(null, testCase);
      } catch (Throwable throwable) {
        ConfigUtils.rethrow(throwable, "Failed to execute ");
      }
    }
    return ret;
  }

  private int registerLabeledTestCases(int id,
      Factors factors,
      Iterable<LabeledTestCase> labeledTestCases,
      TestCaseType testCaseType,
      List<FrameworkMethod> preconditionMethods)
      throws Throwable {
    for (LabeledTestCase labeledTestCase : labeledTestCases) {
      if (shouldPerform(labeledTestCase.getTestCase(), preconditionMethods)) {
        runners.add(new JCUnitRunner(
            getTestClass().getJavaClass(),
            id,
            testCaseType,
            factors,
            labeledTestCase.getTestCase()));
      }
      id++;
    }
    return id;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<LabeledTestCase> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
    List<LabeledTestCase> ret = new LinkedList<LabeledTestCase>();
    try {
      for (FrameworkMethod m : customTestCasesMethods) {
        Object r = m.invokeExplosively(null);

        if (r instanceof LabeledTestCase) {
          ret.add((LabeledTestCase) r);
        } else if (r instanceof Iterable) {
          for (Object o : (Iterable) r) {
            if (o instanceof LabeledTestCase) {
              ret.add((LabeledTestCase) o);
            } else {
              ConfigUtils.checkEnv(false, "Returned value of '%s' must contain only LabeledTestCase objects.");
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
      if (validator.validate(m)) {
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
    List<Throwable> throwables = new LinkedList<Throwable>();
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
    for (Throwable t : throwables) {
      failures.add(t.getMessage());
    }
  }

  /**
   * Returns a {@code Method} object or {@code null} if the specified method is not found or not loadable.
   */
  static Method getTestPreconditionMethod(Class<?> testClass, String methodName, List<String> failures) {
    try {
      return testClass.getMethod(methodName, Tuple.class);
    } catch (NoSuchMethodException e) {
      failures.add(String.format(
          "The method '%s(Tuple)' can't be found in the test class '%s'.",
          methodName,
          Given.class.getSimpleName(),
          testClass.getName()
      ));
      return null;
    }
  }


  public static interface FrameworkMethodValidator {
    public static final FrameworkMethodValidator CUSTOM_TESTCASES = new FrameworkMethodValidator() {
      @Override
      public boolean validate(FrameworkMethod m) {
        Method mm = m.getMethod();
        return m.isPublic() && m.isStatic() && mm.getParameterTypes().length == 0 &&
            (LabeledTestCase.class.isAssignableFrom(mm.getReturnType()) ||
                Iterable.class.isAssignableFrom(mm.getReturnType()));
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
      public boolean validate(FrameworkMethod m) {
        Method mm = m.getMethod();
        boolean ret = true;
        ret &= mm.getParameterTypes().length == 1;
        ret &= Tuple.class.isAssignableFrom(mm.getParameterTypes()[0]);
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
        return "public, static, returning boolean, accepting a Tuple as the first parameter";
      }
    };

    public boolean validate(FrameworkMethod m);

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
