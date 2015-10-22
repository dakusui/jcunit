package com.github.dakusui.jcunit.tests.fsm.simple;

import com.github.dakusui.jcunit.standardrunner.annotations.FactorField;
import com.github.dakusui.jcunit.standardrunner.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.ututils.Metatest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

public class SimpleFSMTest {
  @Test
  public void testValueReturningActionIsPerformedExpectedly() {
    new ValueReturningActionIsPerformedExpectedly().runTests();
  }

  @Test
  public void testExceptionThrowingActionIsPerformedExpectedly() {
    new ExceptionThrowingActionIsPerformedExpectedly().runTests();
  }

  public abstract static class Base extends Metatest {
    public Base(
        int expectedRunCount,
        int expectedFailureCount,
        int expectedIgnoreCount) {
      super(expectedRunCount, expectedFailureCount, expectedIgnoreCount);
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

    public ValueReturningActionIsPerformedExpectedly() {
      super(1, 0, 0);
    }

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

    public ExceptionThrowingActionIsPerformedExpectedly() {
      super(1, 0, 0);
    }

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
