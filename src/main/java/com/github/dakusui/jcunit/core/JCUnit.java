package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
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
    for (int i = 0; i < testCaseGenerator.size(); i++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          testCaseGenerator, i));
    }
    ConstraintManager cm = testCaseGenerator.getConstraintManager();

  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }
}
