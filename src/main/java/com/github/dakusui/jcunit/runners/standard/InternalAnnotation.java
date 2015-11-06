package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.core.TestSuite;

import java.lang.annotation.Annotation;

/**
 * An interface to communicate with JCUnit based rules (subclasses of {@code BaseRule}
 */
public class InternalAnnotation implements Annotation {

  private final TestSuite  testSuite;
  private final TestCase   testCase;
  private final Factors    factors;
  private final Constraint constraint;

  public InternalAnnotation(Factors factors, Constraint constraint, TestSuite suite, TestCase testCase) {
    this.factors = Checks.checknotnull(factors);
    this.constraint = Checks.checknotnull(constraint);
    this.testSuite = Checks.checknotnull(suite);
    // Test case can be null if describeChild is invoked in 'JCUnit' level (not in JCUnitRunner level).
    this.testCase = testCase;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return this.getClass();
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

  public Constraint getConstraint() {
    return this.constraint;
  }
}
