package com.github.dakusui.jcunit.testutils.behaviour;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class BehaviourTestBase<SUT> {
  private final TestScenario testScenario;

  public BehaviourTestBase(TestScenario.Given<SUT> given, TestScenario.When<SUT> when, TestScenario.Then then) {
    this.testScenario = new TestScenario<SUT>(given, when, then);
  }

  @Parameterized.Parameters
  public static Object[][] testScenarios() {
    return new Object[][] {
        { "Hello", "world" }
    };
  }

  @Test
  public void test() {
    this.testScenario.execute();
  }

}
