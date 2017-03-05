package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.lang.annotation.Annotation;

/**
 * An interface to communicate with JCUnit based rules (subclasses of {@code BaseRule}
 */
public class InternalAnnotation implements Annotation {

  private final TestSuite               testSuite;
  private final JCUnit.NumberedTestCase testCase;
  private final Factors                 factors;
  private final ConstraintChecker       constraintChecker;

  public InternalAnnotation(Factors factors, ConstraintChecker constraintChecker, TestSuite suite, JCUnit.NumberedTestCase testCase) {
    this.factors = Checks.checknotnull(factors);
    this.constraintChecker = Checks.checknotnull(constraintChecker);
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

  public JCUnit.NumberedTestCase getTestCase() {
    return this.testCase;
  }

  public ConstraintChecker getConstraintChecker() {
    return this.constraintChecker;
  }
}
