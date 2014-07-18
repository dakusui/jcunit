package com.github.dakusui.jcunit.core;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.Serializable;

public class JCUnitTestDesc extends TestWatcher {
  private String                   testName;
  private JCUnitTestCaseIdentifier id;

  @Override
  protected void starting(Description d) {
    JCUnitTestCaseInternalAnnotation ann = d.getAnnotation(JCUnitTestCaseInternalAnnotation.class);
    Utils.checknotnull(ann, "This class(%s) should be used with classes annotated @RunWith(%s.class)", this.getClass(), JCUnit.class.getClass());
    testName = d.getMethodName();
    id = ann.getId();
  }

  public JCUnitTestCaseType getType() {
    return this.id.testType;
  }

  public Serializable getSubIdentifier() {
    return this.id.idInType;
  }

  public String getTestName() {
    return this.testName;
  }
}
