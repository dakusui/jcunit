package com.github.dakusui.jcunit.examples.fsm.simple;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class SimpleFSMTest {
  public abstract static class Base {
    public int failureCount = 0;
    public int runCount     = 0;
    public int ignoreCount  = 0;

    public void runTests() {
      Result result = JUnitCore.runClasses(this.getClass());
      assertEquals(failureCount, result.getFailureCount());
      assertEquals(runCount, result.getRunCount());
      assertEquals(ignoreCount, result.getIgnoreCount());
    }

    @Test
    public void test() {
      FSMUtils.performStory(this, "brokenFSM", new SimpleFSM(), ScenarioSequence.Observer.Factory.ForSilent.INSTANCE);
    }
  }

  @RunWith(JCUnit.class)
  public static class ValueReturningActionIsPerformedExpectedly extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> valueReturningAction(Expectation.Builder<SimpleFSM> builder) {
        return builder.valid(I, CoreMatchers.is(true)).build();
      }

      @Override
      public boolean check(SimpleFSM simpleFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class ExceptionThrowingActionIsPerformedExpectedly extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> exceptionThrowingAction(Expectation.Builder<SimpleFSM> builder) {
        return builder.invalid(I, RuntimeException.class).build();
      }

      @Override
      public boolean check(SimpleFSM simpleFSM) {
        return true;
      }
    }
  }
}
