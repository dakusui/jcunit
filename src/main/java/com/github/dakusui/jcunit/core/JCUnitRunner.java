package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final Tuple testCase;

  private final int currentTestCaseId;

  private final Object violationId;

  JCUnitRunner(Class<?> type, Tuple testCase, int i, Object violationId)
      throws InitializationError {
    super(type);
    Utils.checknotnull(testCase);
    this.testCase = testCase;
    currentTestCaseId = i;
    // Violation ID can be null for normal cases.
    this.violationId = violationId;
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  @Override
  public Object createTest() throws Exception {
    TestClass klazz = getTestClass();
    Object ret = klazz.getJavaClass().newInstance();
    Utils.initializeTestObject(ret, testCase);
    return ret;
  }

  @Override
  protected String getName() {
    return String.format("[%s]", currentTestCaseId);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    String category = violationId == null ? "normal" : String.format("violation:%s", violationId);
    return String.format("%s(%s)[%s]", method.getName(), category, currentTestCaseId);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }
}