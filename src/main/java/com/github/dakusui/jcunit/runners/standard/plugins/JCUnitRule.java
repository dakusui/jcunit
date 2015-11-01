package com.github.dakusui.jcunit.runners.standard.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.core.TestSuite;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.JCUnitRunner;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

public abstract class JCUnitRule extends TestWatcher {
  private Class<?>  testClass;
  private String    testName;
  private Factors   factors;
  private TestSuite testSuite;
  private TestCase  testCase;

  @Override
  protected void starting(Description d) {
    JCUnitRunner.InternalAnnotation ann = d
        .getAnnotation(JCUnitRunner.InternalAnnotation.class);
    Checks.checknotnull(ann,
        "This class(%s) should be used with classes annotated @%s(%s.class)",
        this.getClass(), RunWith.class, JCUnit.class);
    this.testClass = d.getTestClass();
    this.testName = d.getMethodName();
    this.factors = ann.getFactors();
    this.testSuite = ann.getTestSuite();
    this.testCase = ann.getTestCase();
  }

  public Class<?> getTestClass() {
    return this.testClass;
  }

  public String getTestName() {
    return this.testName;
  }

  public Factors getFactors() {
    return this.factors;
  }

  public TestSuite getTestSuite() {
    return this.testSuite;
  }

  public TestCase getTestCase() {
    return this.testCase;
  }
}
