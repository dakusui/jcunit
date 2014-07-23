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
  private final int id;
  private final JCUnitTestCaseIdentifier labels;

  JCUnitRunner(Class<?> clazz, JCUnitTestCaseType testType, int id, List<Serializable> labels, Tuple testCase)
      throws InitializationError {
    super(clazz);
    Utils.checknotnull(testCase);
    this.testCase = testCase;
    this.id = id;
    this.labels = new JCUnitTestCaseIdentifier(testType, labels);
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
  protected String getName(){
    return String.format("[%d]", this.id);
  }

  @Override
  protected String testName(final FrameworkMethod method) {
    return String.format("%s[%d]", method.getName(),  this.id);
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
    annotations.add(new JCUnitTestCaseInternalAnnotation(this.labels));
    Collections.addAll(annotations, work);
    return Description.createTestDescription(getTestClass().getJavaClass(),
        testName(method), annotations.toArray(new Annotation[annotations.size()]));
  }
}