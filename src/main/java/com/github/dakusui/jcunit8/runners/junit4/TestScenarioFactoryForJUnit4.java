package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.TestScenario;
import org.junit.runners.model.TestClass;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class TestScenarioFactoryForJUnit4 implements TestScenario.Factory {
  private final TestClass testClass;

  public TestScenarioFactoryForJUnit4(TestClass testClass) {
    this.testClass = Objects.requireNonNull(testClass);
  }

  @Override
  public TestScenario create() {
    try {
      return InternalUtils.creteTestScenario(this.testClass, this.testClass.getOnlyConstructor().newInstance());
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      throw Checks.wrap(e);
    }
  }
}
