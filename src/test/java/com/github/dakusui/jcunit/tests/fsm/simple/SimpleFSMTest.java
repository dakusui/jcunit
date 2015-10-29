package com.github.dakusui.jcunit.tests.fsm.simple;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
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

  @Test
  public void testStateCheckingFails() {
    new CheckFails().runTests();
  }

  @Test
  public void testExceptionThrownUnexpectedly() {
    new ExceptionThrownUnexpectedly().runTests();
  }

  @Test
  public void testValueReturnedUnexpectedly() {
    new ValueReturnedUnexpectedly().runTests();
  }

  public abstract static class Base extends Metatest {
    public Base(
        int expectedRunCount,
        int expectedFailureCount,
        int expectedIgnoreCount) {
      super(expectedRunCount, expectedFailureCount, expectedIgnoreCount);
    }

    @Before
    public void before() {
      UTUtils.configureStdIOs();
    }

    @Test
    public void test() {
      FSMUtils.performStory(
          this,
          "brokenFSM",
          new SimpleFSM(),
          new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.stdout())
      );
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
    @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Value({"1"}))
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

  @RunWith(JCUnit.class)
  public static class CheckFails extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Value({"1"}))
    public Story<SimpleFSM, Spec> brokenFSM;

    public CheckFails() {
      super(1, 1, 0);
    }

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> exceptionThrowingAction(Expectation.Builder<SimpleFSM> builder) {
        return builder.invalid(I, RuntimeException.class).build();
      }

      @Override
      public boolean check(SimpleFSM simpleFSM) {
        return false;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class ExceptionThrownUnexpectedly extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Value({"1"}))
    public Story<SimpleFSM, Spec> brokenFSM;

    public ExceptionThrownUnexpectedly() {
      super(1, 1, 0);
    }

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> exceptionThrowingAction(Expectation.Builder<SimpleFSM> builder) {
        return builder.valid(I).build();
      }

      @Override
      public boolean check(SimpleFSM simpleFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class ValueReturnedUnexpectedly extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Value({"1"}))
    public Story<SimpleFSM, Spec> brokenFSM;

    public ValueReturnedUnexpectedly() {
      super(1, 1, 0);
    }

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> valueReturningAction(Expectation.Builder<SimpleFSM> builder) {
        // Use out of memory error to avoid matching with unintentionally
        // thrown exception
        return builder.invalid(I, OutOfMemoryError.class).build();
      }

      @Override
      public boolean check(SimpleFSM simpleFSM) {
        return true;
      }
    }
  }
}
