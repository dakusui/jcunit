package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final Tuple               testCase;
  private final int                 id;
  private final JCUnit.TestCaseType type;
  private final Factors             factors;

  JCUnitRunner(Class<?> clazz, int id, JCUnit.TestCaseType testType,
      Factors factors, Tuple testCase)
      throws InitializationError {
    super(clazz);
    Utils.checknotnull(testCase);
    this.factors = factors;
    this.testCase = testCase;
    this.id = id;
    this.type = testType;
  }

  /**
   * Returns {@code null}, if the list {@code whenMethods} is empty, which means
   * all test cases should be executed.
   * <p/>
   * An empty set returned by this method means no test method should be executed
   * for the given {@code testCase}.
   */
  private static boolean shouldInvoke(Object testObject,
      List<FrameworkMethod> preconditions) {
    if (preconditions == null) {
      return true;
    }
    List<String> failures = new LinkedList<String>();
    for (FrameworkMethod each : preconditions) {
      try {
        if ((Boolean) each.invokeExplosively(null, testObject)) {
          return true;
        }
      } catch (Throwable throwable) {
        ConfigUtils.rethrow(throwable,
            "Failed to invoke test precondition method '%s'(%s)",
            each.getName(),
            each.getDeclaringClass().getCanonicalName()
        );
      }
    }
    Utils.checkcond(failures.isEmpty(), "Some errors are detected.: %s",
        failures);
    return false;
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
    Method m = JCUnit
        .getTestPreconditionMethod(testClass, methodName, failures);
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
   * Without overriding this method, all the tests will fail for 'AssertionError',
   * because {@code {@literal @}BeforeClass} methods and {@code {@literal @}AfterClass}
   * methods are executed for every test case run not before and after all the
   * test cases are executed.
   *
   * {@code BlockJUnit4ClassRunnerWithParameters} does the same.
   *
   * @see org.junit.runners.BlockJUnit4ClassRunner#classBlock(org.junit.runner.notification.RunNotifier)
   */
  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  /**
   * Overrides super class's {@code createTest()} method, which throws a {@code java.lang.Exception},
   * to simplify exception handling.
   */
  @Override
  public Object createTest() {
    TestClass klazz = getTestClass();
    return JCUnit.createTest(klazz, testCase);
  }

  @Override
  protected String getName() {
    return String.format("[%d]", this.id);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%d]", method.getName(), this.id);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    Utils.checknotnull(method);

    Annotation[] work = method.getAnnotations();
    ArrayList<Annotation> annotations = new ArrayList<Annotation>(
        work.length + 1);
    annotations.add(
        new JCUnit.TestCaseInternalAnnotation(this.type, this.id, this.factors,
            this.testCase));
    Collections.addAll(annotations, work);
    return Description.createTestDescription(getTestClass().getJavaClass(),
        testName(method),
        annotations.toArray(new Annotation[annotations.size()]));
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    for (FrameworkMethod each : this.computeTestMethods()) {
      assert this.testCase != null;
      if (this.shouldInvoke(each, createTest())) {
        ret.add(each);
      }
    }
    if (ret.isEmpty()) {
      throw new RuntimeException(String
          .format("No matching test method is found for test: %s",
              this.testCase));
    }
    return ret;
  }

  private boolean shouldInvoke(FrameworkMethod testMethod, Object testObject) {
    List<String> failures = new LinkedList<String>();
    List<FrameworkMethod> preconditionMethods = getTestPreconditionMethodsFor(
        testMethod, failures);
    ConfigUtils.checkTest(failures.isEmpty(),
        "Errors are found while precondition checks.: %s", failures);
    return shouldInvoke(testObject, preconditionMethods);
  }

  /**
   * Returns {@code null}, if the {@code testMethod} doesn't have 'When' annotation,
   * which means the method should be executed without any preconditions.
   */
  private List<FrameworkMethod> getTestPreconditionMethodsFor(
      FrameworkMethod testMethod, List<String> failures) {
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    Class<?> testClass = getTestClass().getJavaClass();
    Given given = testMethod.getAnnotation(Given.class);

    if (given == null) {
      return null;
    }

    for (String methodName : given.value()) {
      FrameworkMethod m = getTestPreconditionMethod(testClass, methodName,
          testMethod, failures);
      if (m != null) {
        ret.add(m);
      }
    }

    for (FrameworkMethod each : ret) {
      JCUnit.validateTestPreconditionMethod(testClass, each, failures);
    }
    return ret;
  }
}