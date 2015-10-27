package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.runners.standard.annotations.TupleGeneration;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
      List<String> errors = new LinkedList<String>();
      List<FrameworkMethod> preconditionMethods = getFrameworkMethods(FrameworkMethodUtils.FrameworkMethodRetriever.PRECONDITION);
      for (FrameworkMethod each : preconditionMethods) {
        FrameworkMethodUtils.validateFrameworkMethod(klass, each, FrameworkMethodUtils.FrameworkMethodValidator.PRECONDITIONMETHOD_VALIDATOR, errors);
      }
      // Currently only one filter method can be used.
      // Custom test case methods.
      List<FrameworkMethod> customTestCaseMethods = getFrameworkMethods(FrameworkMethodUtils.FrameworkMethodRetriever.CUSTOM_TESTCASES);
      for (FrameworkMethod each : customTestCaseMethods) {
        FrameworkMethodUtils.validateFrameworkMethod(klass, each, FrameworkMethodUtils.FrameworkMethodValidator.CUSTOMTESTCASEMETHOD_VALIDATOR, errors);
      }
      ////
      // Check if any error was found.
      Checks.checkenv(errors.isEmpty(),
          "Errors are found in test class '%s':%s",
          getTestClass().getJavaClass().getCanonicalName(),
          errors);

      ////
      // Generate a list of test cases using a specified tuple generator
      TupleGenerator tupleGenerator = getTupleGeneratorFactory()
          .createFromClass(klass);
      Factors factors = tupleGenerator.getFactors();
      int id;
      for (id = (int) tupleGenerator.firstId();
           id >= 0; id = (int) tupleGenerator.nextId(id)) {
        Tuple testCase = tupleGenerator.get(id);
        if (shouldPerform(testCase, preconditionMethods)) {
          runners.add(createRunner(id, factors, TestCaseType.Generated, testCase));
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
      Checks.checkenv(runners.size() > 0, "No test to be run was found.");
    } catch (JCUnitException e) {
      throw tryToRecreateRootCauseException(Checks.getRootCauseOf(e), e.getMessage());
    }
  }

  private static Throwable tryToRecreateRootCauseException(Throwable rootCause, String message) {
    rootCause = Checks.checknotnull(rootCause);
    if (message == null)
      return rootCause;
    Throwable ret = null;
    try {
      ret = ReflectionUtils.create(rootCause.getClass(), new ReflectionUtils.TypedArg(String.class, message));
      ret.setStackTrace(rootCause.getStackTrace());
    } finally {
      ////
      // Exception thrown during re-instantiate originally thrown one should
      // be ignored. Intentionally masked.
      if (ret != null) //noinspection ReturnInsideFinallyBlock
        return ret;
      else //noinspection ReturnInsideFinallyBlock
        return rootCause;
    }
  }

  protected TupleGeneration.TupleGeneratorFactory getTupleGeneratorFactory() {
    return TupleGeneration.TupleGeneratorFactory.INSTANCE;
  }

  static Object createTestObject(TestClass testClass, Tuple testCase) {
    return TestCaseUtils.toTestObject(testClass.getJavaClass(), testCase);
  }

  private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
    if (preconditionMethods.isEmpty()) {
      return true;
    }
    for (FrameworkMethod m : preconditionMethods) {
      try {
        Object testObject = createTestObject(this.getTestClass(),
            testCase);
        if ((Boolean) m.invokeExplosively(testObject)) {
          return true;
        }
      } catch (Throwable throwable) {
        throw Checks.wrap(throwable, "Failed to execute ");
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
        runners.add(createRunner(id, factors, testCaseType, testCase));
      }
      id++;
    }
    return id;
  }

  protected JCUnitRunner createRunner(int id, Factors factors, TestCaseType testCaseType, Tuple testCase) throws InitializationError {
    return new JCUnitRunner(
        getTestClass().getJavaClass(),
        id,
        testCaseType,
        factors,
        testCase);
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<Tuple> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (FrameworkMethod each : customTestCasesMethods) {
      try {
        Object r = each.invokeExplosively(null);
        if (r instanceof Iterable) {
          for (Object o : (Iterable) r) {
            addTestCase(o, ret, each);
          }
        } else {
          addTestCase(r, ret, each);
        }
      } catch (Throwable throwable) {
        throw Checks.wrap(throwable, "Failed to execute '%s'.", each.getName());
      }
    }
    return ret;
  }

  /**
   * Add test case to {@code tupleList}.
   * It will be converted to a tuple, if necessary.
   *
   * @param testCase        A test case object. Type is unknown.
   * @param tupleList       A list to test case tuple to add to.
   * @param frameworkMethod A framework method from which {@code testCase} is returned.
   */
  private void addTestCase(Object testCase, List<Tuple> tupleList, FrameworkMethod frameworkMethod) {
    Checks.checknotnull(
        testCase,
        "null is returned (or contained in a collection returned) by '%s.%s' (in %s)",
        frameworkMethod.getDeclaringClass(),
        frameworkMethod.getName()
    );

    if (testCase instanceof Tuple) {
      tupleList.add((Tuple) testCase);
    } else if (getTestClass().getJavaClass().isAssignableFrom(testCase.getClass())) {
      tupleList.add(TestCaseUtils.toTestCase(testCase));
    } else {
      Checks.checkcond(
          false,
          "Unknown type object (%s) is returned by '%s' (in %s)",
          testCase,
          frameworkMethod.getName(),
          frameworkMethod.getDeclaringClass()
      );
    }
  }

  private List<FrameworkMethod> getFrameworkMethods(FrameworkMethodUtils.FrameworkMethodRetriever retriever) {
    return retriever.getMethods(getTestClass().getJavaClass());
  }


  /**
   * Identifies what kind of category to which a test case belongs.
   */
  public enum TestCaseType {
    /**
     * A custom test case, which is returned by a method annotated with {@literal @}{@code CustomTestCases}.
     */
    Custom,
    /**
     * A generated test case. A test case generated by JCUnit framework through an implementation of {@code TupleGenerator}
     * belongs to this category.
     */
    Generated,
    /**
     * A test case which violates some defined constraint belongs to this category.
     * Test cases returned by {@code ConstraintManager#getViolations} belongs to this.
     */
    Violation
  }

  public static class InternalAnnotation implements Annotation {

    private final TestCaseType type;
    private final int          id;
    private       Factors      factors;
    private       Tuple        testCase;

    public InternalAnnotation(TestCaseType type, int id, Factors factors,
        Tuple testCase) {
      Checks.checknotnull(type);
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
}
