package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.Violation;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import com.github.dakusui.jcunit.generators.TestCaseGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    TestCaseGenerator testCaseGenerator = TestCaseGeneratorFactory.INSTANCE
        .createTestCaseGenerator(klass);
    int id;
    for (id = 0; id < testCaseGenerator.size(); id++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          JCUnitTestCaseType.Normal, id, testCaseGenerator.get(id)));
    }
    ConstraintManager cm = testCaseGenerator.getConstraintManager();
    final List<Violation> violations = cm.getViolations();
    for (int i = 0; i < violations.size(); i++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          JCUnitTestCaseType.Violation, violations.get(i).getId(),
          violations.get(i).getTestCase()));
      id++;
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }
}
