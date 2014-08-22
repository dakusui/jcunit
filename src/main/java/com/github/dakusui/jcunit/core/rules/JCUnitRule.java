package com.github.dakusui.jcunit.core.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public abstract class JCUnitRule extends TestWatcher {
  private Class<?>            testClass;
  private String              testName;
  private Factors             factors;
  private int                 id;
  private JCUnit.TestCaseType type;
  private Tuple               testCase;

  @Override
  protected void starting(Description d) {
    JCUnit.InternalAnnotation ann = d
        .getAnnotation(JCUnit.InternalAnnotation.class);
    Utils.checknotnull(ann,
        "This class(%s) should be used with classes annotated @RunWith(%s.class)",
        this.getClass(), JCUnit.class.getClass());
    this.testClass = d.getTestClass();
    this.testName = d.getMethodName();
    this.factors = ann.getFactors();
    this.id = ann.getId();
    this.type = ann.getTestCaseType();
    this.testCase = TupleUtils.unmodifiableTuple(ann.getTestCase());
  }

  public JCUnit.TestCaseType getTestCaseType() {
    return this.type;
  }

  public Class<?> getTestClass() {
    return this.testClass;
  }

  public String getTestName() {
    return this.testName;
  }

  public Tuple getTestCase() {
    return this.testCase;
  }

  public int getId() {
    return this.id;
  }

  public Factors getFactors() {
    return this.factors;
  }
}
