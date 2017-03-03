package com.github.dakusui.jcunit.tests.features.fsm.simple;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;

public class BrokenFSMTest {
  @Test
  public void testTrueShouldBeReturnedButFalse() {
    new TrueShouldBeReturnedButFalse().runTests();
  }

  @Test
  public void testTrueShouldBeReturnedButException() {
    new TrueShouldBeReturnedButException().runTests();
  }

  @Test
  public void testExceptionShouldBeThrownButTrueReturned() {
    new ExceptionShouldBeThrownButTrueReturned().runTests();
  }

  @Test
  public void testRuntimeExceptionShouldBeThrownButFileNotFoundThrown() {
    new RuntimeExceptionShouldBeThrownButFileNotFoundThrown().runTests();
  }

  @Test
  public void testInitialStateShouldBeIButNot() {
    new InitialStateShouldBeIButNot().runTests();
  }

  @Test
  public void testStateAfterSuccessfulActionShouldBeJButNot() {
    new StateAfterValueReturningActionShouldBeJButNot().runTests();
  }

  @Test
  public void testStateAfterExceptionThrowingActionShouldBeJButNot() {
    new StateAfterExceptionThrowingActionShouldBeJButNot().runTests();
  }

  public static class Base extends SimpleFSMTest.Base {
    public Base() {
      this(1, 1, 0);
      // expectedFailureCount = 1;
      // expectedRunCount = 1;
      // expectedIgnoreCount = 0;
    }

    public Base(
        int expectedRunCount,
        int expectedFailureCount,
        int expectedIgnoreCount) {
      super(expectedRunCount, expectedFailureCount, expectedIgnoreCount);
    }
  }

  @RunWith(JCUnit.class)
  public static class TrueShouldBeReturnedButFalse extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> shouldReturnTrueButReturnsFalse(Expectation.Builder<SimpleFSM> b) {
        return b.valid(I, CoreMatchers.is(true)).build();
      }

      @Override
      public boolean check(SimpleFSM brokenFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class TrueShouldBeReturnedButException extends Base {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> shouldReturnTrueButThrowsException(Expectation.Builder<SimpleFSM> b) {
        return b.valid(I, CoreMatchers.is(true)).build();
      }

      @Override
      public boolean check(SimpleFSM brokenFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class ExceptionShouldBeThrownButTrueReturned extends Base {

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> shouldThrowsExceptionButReturnsTrue(Expectation.Builder<SimpleFSM> b) {
        return b.invalid(I, FileNotFoundException.class).build();
      }

      @Override
      public boolean check(SimpleFSM brokenFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class RuntimeExceptionShouldBeThrownButFileNotFoundThrown extends Base {

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> shouldThrowRuntimeExceptionButFileNotFoundThrown(Expectation.Builder<SimpleFSM> b) {
        return b.invalid(I, RuntimeException.class).build();
      }

      @Override
      public boolean check(SimpleFSM brokenFSM) {
        return true;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class InitialStateShouldBeIButNot extends Base {

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I;

      @ActionSpec
      public Expectation<SimpleFSM> successfulAction(Expectation.Builder<SimpleFSM> b) {
        return b.valid(I).build();
      }

      @Override
      public boolean check(SimpleFSM brokenFSM) {
        // Returns false to break a test.
        return false;
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class StateAfterValueReturningActionShouldBeJButNot extends Base {

    public StateAfterValueReturningActionShouldBeJButNot() {
      // expectedRunCount = 2;
      // expectedFailureCount = 2;
      // expectedIgnoreCount = 0;
      super(2, 2, 0);
    }

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I {
        @Override
        public boolean check(SimpleFSM brokenFSM) {
          return true;
        }
      },
      @StateSpec J {
        @Override
        public boolean check(SimpleFSM brokenFSM) {
          return false;
        }
      };

      @ActionSpec
      public Expectation<SimpleFSM> valueReturningAction(Expectation.Builder<SimpleFSM> b) {
        return b.valid(J).build();
      }
    }
  }

  @RunWith(JCUnit.class)
  public static class StateAfterExceptionThrowingActionShouldBeJButNot extends Base {

    public StateAfterExceptionThrowingActionShouldBeJButNot() {
      // expectedRunCount = 2;
      // expectedFailureCount = 2;
      // expectedIgnoreCount = 0;
      super(2, 2, 0);
    }

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<SimpleFSM, Spec> brokenFSM;

    public enum Spec implements FSMSpec<SimpleFSM> {
      @StateSpec I {
        @Override
        public boolean check(SimpleFSM brokenFSM) {
          return true;
        }
      },
      @StateSpec J {
        @Override
        public boolean check(SimpleFSM brokenFSM) {
          return false;
        }
      };

      @ActionSpec
      public Expectation<SimpleFSM> exceptionThrowingAction(Expectation.Builder<SimpleFSM> b) {
        return b.invalid(J, RuntimeException.class).build();
      }
    }
  }
}
