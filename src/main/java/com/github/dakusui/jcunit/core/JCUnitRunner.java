package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
  private final Tuple testCase;
  private final JCUnitTestCaseIdentifier id;

  JCUnitRunner(Class<?> clazz, JCUnitTestCaseType testType, Serializable idInType, Tuple testCase)
      throws InitializationError {
    super(clazz);
    Utils.checknotnull(testCase);
    this.testCase = testCase;
    id = new JCUnitTestCaseIdentifier(testType, idInType);
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
  protected String testName(final FrameworkMethod method) {
    return String.format("%s.%s(%s)", this.getTestClass().getJavaClass().getSimpleName(), method.getName(),  this.id.toString());
  }

  @Override
  protected void validateConstructor(List<Throwable> errors) {
    validateZeroArgConstructor(errors);
  }


  @Override
  protected Description describeChild(FrameworkMethod method) {
    Utils.checknotnull(method);

    Annotation[] work = method.getAnnotations();
    ArrayList<Annotation> annotations = new ArrayList<Annotation>(work.length + 1);
    annotations.add(new JCUnitTestCaseInternalAnnotation(this.id));
    Collections.addAll(annotations, work);
    return Description.createTestDescription(getTestClass().getJavaClass(),
        testName(method), annotations.toArray(new Annotation[annotations.size()]));
  }

}