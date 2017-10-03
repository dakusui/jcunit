package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testsuite.TestScenarioBk;
import org.junit.runners.model.TestClass;

import java.util.Objects;

public class TestScenarioFactoryForJUnit4 implements TestScenarioBk.Factory {
  private final TestClass testClass;

  public TestScenarioFactoryForJUnit4(TestClass testClass) {
    this.testClass = Objects.requireNonNull(testClass);
  }

  @Override
  public TestScenarioBk create(Tuple testInput) {
    return null;
  }
}
