package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final TestCaseGenerator testCases;

  private final int currentTestCaseId;

  JCUnitRunner(Class<?> type, TestCaseGenerator testCases, int i)
      throws InitializationError {
    super(type);
    this.testCases = testCases;
    currentTestCaseId = i;
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  @Override
  public Object createTest() throws Exception {
    TestClass klazz = getTestClass();
    Object ret = klazz.getJavaClass().newInstance();
    Tuple values = testCases.get(currentTestCaseId);
    Utils.initializeTestObject(ret, values);
    return ret;
  }

  @Override
  protected String getName() {
    return String.format("[%s]", currentTestCaseId);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%s]", method.getName(), currentTestCaseId);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }
}