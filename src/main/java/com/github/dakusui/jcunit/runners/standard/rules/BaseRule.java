package com.github.dakusui.jcunit.runners.standard.rules;

import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.InternalAnnotation;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

/**
 * A model class for test rules that access JCUnit's runtime information such as,
 * test name, test case tuple, test case type, test suite structure.
 *
 * You can extend this class to write your own rules for JCUnit.
 *
 * For available extension points like {@code starting}, {@code skipped}, {@code finished},
 * etc, see {@link TestWatcher}.
 *
 * @see TestWatcher
 */
public abstract class BaseRule extends TestWatcher {
  private Class<?>                testClass;
  private String                  testName;
  private Factors                 factors;
  private ConstraintChecker       constraintChecker;
  private TestSuite               testSuite;
  private JCUnit.NumberedTestCase testCase;

  @Override
  protected void starting(Description d) {
    InternalAnnotation ann = d
        .getAnnotation(InternalAnnotation.class);
    Checks.checknotnull(ann,
        "This class(%s) should be used with classes annotated @%s(%s.class), all visible annotations are following.: %s",
        this.getClass(), RunWith.class, JCUnit.class,
        d.getAnnotations()
    );
    this.testClass = d.getTestClass();
    this.testName = d.getMethodName();
    this.factors = ann.getFactors();
    this.constraintChecker = ann.getConstraintChecker();
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

  public ConstraintChecker getConstraintChecker() {
    return this.constraintChecker;
  }

  public TestSuite getTestSuite() {
    return this.testSuite;
  }

  public JCUnit.NumberedTestCase getTestCase() {
    return this.testCase;
  }
}
