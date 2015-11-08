package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.core.TestSuite;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Parameterized {
  private final List<Runner> runners;

  private final TestSuite testSuite;
  private final Factors   factors;

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    // To suppress unnecessary validation on @Parameters annotated methods,
    // We have overridden createTestClass method. For detail refer to the method.
    super(klass);
    List<FrameworkMethod> preconditionMethods = getTestClass().getAnnotatedMethods(Precondition.class);
    List<FrameworkMethod> customTestCaseMethods = getTestClass().getAnnotatedMethods(CustomTestCases.class);
    RunnerContext runnerContext = new RunnerContext.Base(this.getTestClass().getJavaClass());
    CoveringArrayEngine coveringArrayEngine = new Generator.Base(klass.getAnnotation(GenerateWith.class).generator(), runnerContext).build();
    Constraint constraint = new Checker.Base(klass.getAnnotation(GenerateWith.class).checker(), runnerContext).build();
    // TODO build factor space appropriately #35
    FactorSpace factorSpace = new FactorSpace.Builder().build();
    try {
      ////
      // Generate a list of test cases using a specified tuple generator
      CoveringArray ca = coveringArrayEngine.generate(factorSpace);
      List<TestCase> testCases = Utils.newList();
      int id;
      for (id = ca.firstId();
           id >= 0; id = ca.nextId(id)) {
        Tuple testCase = ca.get(id);
        if (shouldPerform(testCase, preconditionMethods)) {
          testCases.add(
              new TestCase(id, TestCase.Type.Generated, testCase
              ));
        }
      }
      // Skip to number of test cases generated.
      id = ca.size();
      ////
      // Compose a list of 'negative test cases' and register them.
      final Constraint cm = constraint;
      final List<Tuple> violations = cm.getViolations();
      id = registerTestCases(
          testCases,
          id,
          violations,
          TestCase.Type.Violation,
          preconditionMethods);
      ////
      // Compose a list of 'custom test cases' and register them.
      registerTestCases(
          testCases,
          id,
          invokeCustomTestCasesMethod(customTestCaseMethods),
          TestCase.Type.Custom,
          preconditionMethods);
      Checks.checkenv(testCases.size() > 0, "No test to be run was found.");
      ////
      // Create and hold a test suite object to use it in rules.
      this.factors = factorSpace.factors;
      this.testSuite = new TestSuite(testCases);
      this.runners = Utils.transform(
          this.testSuite,
          new Utils.Form<TestCase, Runner>() {

            @Override
            public Runner apply(TestCase in) {
              try {
                return new JCUnitRunner(
                    getTestClass().getJavaClass(),
                    factors,
                    cm,
                    testSuite,
                    in
                );
              } catch (InitializationError initializationError) {
                throw Checks.wrap(initializationError);
              }
            }
          });

    } catch (JCUnitException e) {
      throw tryToRecreateRootCauseException(Checks.getRootCauseOf(e), e.getMessage());
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> clazz) {
    return new TestClass(clazz) {
      public List<FrameworkMethod> getAnnotatedMethods(
          Class<? extends Annotation> annotationClass) {
        if (Parameterized.Parameters.class.equals(annotationClass)) {
          return Collections.singletonList(new FrameworkMethod(ReflectionUtils.getMethod(DummyMethodHolderForParameterizedRunner.class, "dummy")));

        }
        return super.getAnnotatedMethods(annotationClass);
      }
    };
  }

  protected GenerateWith.CoveringArrayEngineFactory getCoveringArrayEngineFactory() {
    return GenerateWith.CoveringArrayEngineFactory.INSTANCE;
  }

  private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
    if (preconditionMethods.isEmpty()) {
      return true;
    }
    for (FrameworkMethod m : preconditionMethods) {
      try {
        Object testObject = TestCaseUtils.toTestObject(
            this.getTestClass().getJavaClass(),
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

  private int registerTestCases(
      List<TestCase> testCases,
      int id,
      Iterable<Tuple> testCaseTuplesToBeAdded,
      TestCase.Type type,
      List<FrameworkMethod> preconditionMethods)
      throws Throwable {
    for (Tuple testCase : testCaseTuplesToBeAdded) {
      if (shouldPerform(testCase, preconditionMethods)) {
        testCases.add(new TestCase(id, type, testCase));
      }
      id++;
    }
    return id;
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


  /**
   * A class referenced by createTestClass method.
   * This is only used to mock JUnit's Parameterized runner.
   */
  public static class DummyMethodHolderForParameterizedRunner {
    @SuppressWarnings("unused") // This method is referenced reflectively.
    @Parameters
    public static Object[][] dummy() {
      return new Object[][] { {} };
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
      if (ret == null)
        ret = rootCause;
    }
    return ret;
  }
}
