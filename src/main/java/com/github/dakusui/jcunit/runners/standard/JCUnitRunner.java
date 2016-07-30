package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.annotations.Uses;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Runs each test case.
 */
public class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final Factors                          factors;
  private final TestSuite                        testSuite;
  private final JCUnit.NumberedTestCase          testCase;
  private final Map<String, FrameworkMethod>     methods;
  private final ConstraintChecker                constraintChecker;
  private final FactorSpace                      factorSpace;
  private final Map<FrameworkMethod, Set<Tuple>> coveredMethods;

  /**
   * Creates an object of this class.
   *
   * @param clazz    A test class.
   * @param testCase A test case object.  @throws InitializationError In case initialization is failed. e.g. More than one constructor is found in the test class.
   */
  public JCUnitRunner(Class<?> clazz, FactorSpace factorSpace, ConstraintChecker constraintChecker, TestSuite testSuite, Map<FrameworkMethod, Set<Tuple>> coveredMethods, JCUnit.NumberedTestCase testCase) throws InitializationError {
    super(clazz);
    this.factorSpace = Checks.checknotnull(factorSpace);
    this.factors = factorSpace.factors;
    this.constraintChecker = Checks.checknotnull(constraintChecker);
    this.testSuite = Checks.checknotnull(testSuite);
    this.coveredMethods = Checks.checknotnull(coveredMethods);
    this.testCase = Checks.checknotnull(testCase);
    TestClass testClass = getTestClass();
    Map<String, FrameworkMethod> methods = new LinkedHashMap<String, FrameworkMethod>();
    for (FrameworkMethod each : testClass.getAnnotatedMethods(When.class)) {
      When when = each.getAnnotation(When.class);

      CompositeFrameworkMethod compositeFrameworkMethod = FrameworkMethodUtils.buildCompositeFrameworkMethod(testClass, when);
      methods.put(compositeFrameworkMethod.getName(), compositeFrameworkMethod);
    }
    this.methods = Collections.unmodifiableMap(methods);
  }

  /**
   * Without overriding this method, all the tests will fail for 'AssertionError',
   * because {@code {@literal @}BeforeClass} methods and {@code {@literal @}AfterClass}
   * methods are executed for every test case run not before and after all the
   * test cases are executed.
   * <p/>
   * {@code BlockJUnit4ClassRunnerWithParameters} does the same.
   *
   * @see org.junit.runners.BlockJUnit4ClassRunner#classBlock(org.junit.runner.notification.RunNotifier)
   */
  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  /**
   * Overrides super class's {@code createTestObject()} method, which throws a {@code java.lang.Exception},
   * to simplify exception handling.
   */
  @Override
  public Object createTest() {
    return TestCaseUtils.toTestObject(getTestClass().getJavaClass(), this.factorSpace.convert(testCase.getTuple()));
  }

  @Override
  protected String getName() {
    return String.format("[%d]", this.testCase.getId());
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%d]", method.getName(), this.testCase.getId());
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    Checks.checknotnull(method);

    String name = testName(method);
    List<? super Annotation> annotations = Utils.<Annotation>newList(new InternalAnnotation(this.factors, this.constraintChecker, this.testSuite, this.testCase));
    annotations.addAll(Utils.asList(method.getAnnotations()));
    ////
    // Elements in the list are all annotations.
    //noinspection SuspiciousToArrayCall
    return Description.createTestDescription(
        getTestClass().getJavaClass(),
        name,
        annotations.toArray(new Annotation[annotations.size()]));
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    if (method.getAnnotation(Ignore.class) != null) {
      notifier.fireTestIgnored(description);
    } else {
      runLeaf(methodBlock(method), description, notifier);
    }
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    for (FrameworkMethod each : computeTestMethods()) {
      if (shouldInvoke(each, createTest()))
        ret.add(each);
    }
    if (ret.isEmpty())
      ret.add(getDummyMethodForNoMatchingMethodFound());
    return ret;
  }

  private boolean shouldInvoke(FrameworkMethod testMethod, Object testObject) {
    ////
    // Check if the method is already invoked with the same values for the parameters
    // used in it.
    Tuple subtupleUsedByTestMethod = subtupleUsedByMethod(testMethod, TestCaseUtils.toTestCase(testObject));
    if (subtupleUsedByTestMethod != null) {
      if (isAlreadyCovered(testMethod, subtupleUsedByTestMethod)) {
        return false;
      }
      markCovered(testMethod, subtupleUsedByTestMethod);
    }

    When when = testMethod.getAnnotation(When.class);
    if (when == null)
      return true;
    String preconditionMethodName = FrameworkMethodUtils.composePreconditionCompositeFrameworkMethodName(when);
    FrameworkMethod preconditionMethod = this.methods.get(preconditionMethodName);
    Checks.checkcond(preconditionMethod != null, "Something went wrong: name=%s, methdos=%s", preconditionMethodName, this.methods);
    boolean ret;
    try {
      ////
      // It's guaranteed that preconditionMethod returns a boolean by validation process.
      ret = (Boolean) preconditionMethod.invokeExplosively(testObject);
    } catch (RuntimeException e) {
      throw e;
    } catch (Error e) {
      throw e;
    } catch (Throwable throwable) {
      throw Checks.wrap(throwable);
    }
    return ret;
  }

  private boolean isAlreadyCovered(FrameworkMethod testMethod, Tuple subtupleUsedByTestMethod) {
    if (subtupleUsedByTestMethod == null)
      return false;
    ensureSubtupleSetRegistered(testMethod);
    return this.coveredMethods.get(testMethod).contains(subtupleUsedByTestMethod);
  }

  private void markCovered(FrameworkMethod testMethod, Tuple subtupleUsedByTestMethod) {
    ensureSubtupleSetRegistered(testMethod);
    this.coveredMethods.get(testMethod).add(subtupleUsedByTestMethod);
  }

  private void ensureSubtupleSetRegistered(FrameworkMethod testMethod) {
    if (!this.coveredMethods.containsKey(testMethod)) {
      this.coveredMethods.put(testMethod, new HashSet<Tuple>());
    }
  }

  private Tuple subtupleUsedByMethod(FrameworkMethod testMethod, Tuple testCase) {
    Uses uses = testMethod.getAnnotation(Uses.class);
    if (uses == null)
      return null;
    Tuple.Builder builder = new Tuple.Builder();
    for (String each : uses.value()) {
      if ("*".equals(each))
        return null;
      builder.put(each, testCase.get(each));
    }
    return builder.build();
  }

  private FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
    try {
      return new FrameworkMethod(JCUnitRunner.class.getMethod("noMatchingTestMethodIsFoundForThisTestCase")) {
        @Override
        public String getName() {
          return String.format("%s:%s", super.getName(), TupleUtils.toString(JCUnitRunner.this.testCase.getTuple()));
        }
      };
    } catch (NoSuchMethodException e) {
      throw Checks.wrap(e);
    }
  }

  /**
   * This method is only used through reflection to let JUnit know the test case is ignored since
   * no matching test method is defined for it.
   *
   * @see JCUnitRunner#getDummyMethodForNoMatchingMethodFound()
   */
  @Ignore
  @SuppressWarnings("unused")
  public static void noMatchingTestMethodIsFoundForThisTestCase() {
  }
}