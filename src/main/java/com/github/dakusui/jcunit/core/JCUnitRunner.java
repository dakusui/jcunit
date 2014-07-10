package com.github.dakusui.jcunit.core;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final List<Tuple> fParameterList;

  private final int fParameterSetNumber;

  JCUnitRunner(Class<?> type, List<Tuple> parameterList, int i)
      throws InitializationError {
    super(type);
    fParameterList = parameterList;
    fParameterSetNumber = i;
  }

  @Override
  protected Statement classBlock(RunNotifier notifier) {
    return childrenInvoker(notifier);
  }

  @Override
  public Object createTest() throws Exception {
    TestClass klazz = getTestClass();
    Object ret = klazz.getJavaClass().newInstance();
    Tuple values = (Tuple) computeParams();
    Utils.initializeTestObject(ret, values);
    return ret;
  }

  @Override
  protected String getName() {
    return String.format("[%s]", fParameterSetNumber);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%s]", method.getName(), fParameterSetNumber);
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }

  private Tuple computeParams() throws Exception {
    return fParameterList.get(fParameterSetNumber);
  }

}