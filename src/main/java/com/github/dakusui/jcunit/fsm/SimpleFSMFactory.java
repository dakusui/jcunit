package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class SimpleFSMFactory<SUT> implements FSMFactory {
  /**
   * Methods that represent state transition functions are annotated with this.
   * A method annotated with this must return a state object and the first parameter
   * of it must always be an object of {@code SUT}.
   *
   * Also, the first parameter must not be annotated with {@code @Parameter} since
   * it is not a parameter.
   */
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Transition {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Parameter {
    /**
     * Must return a name of public static method which returns an array whose
     * type is exactly the same as the parameter to which this annotation is attached.
     */
    public String value();
  }

  public static class Turnstile {
  }

  public static enum ExampleFSM {
    I {
    },
    OK {
      @Override
      public ExampleFSM pass(Turnstile sut) {
        return I;
      }
    },
    NG {
    };

    @Transition
    public ExampleFSM insert(Turnstile sut, @Parameter("coin") int coin) {
      if (coin < 100) return this;
      return OK;
    }

    @Transition
    public ExampleFSM pass(Turnstile sut) {
      throw new IllegalStateException();
    }


    public static int[] coin(Turnstile sut) {
      return new int[] { 1, 5, 10, 50, 100, 500 };
    }
  }

  private final State<SUT>[] states;

  public SimpleFSMFactory(Class<? extends Enum> clazz) {
    Checks.checknotnull(clazz);
    Checks.checktest(
        State.class.isAssignableFrom(clazz),
        "'%s' isn't a state (%s)", clazz.getCanonicalName(), State.class.getCanonicalName()
    );

    try {
      this.states = (State[])clazz.getMethod("values").invoke(null);
    } catch (IllegalAccessException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      throw new JCUnitException(e.getMessage(), e);
    }
  }

  @Override
  public FSM<SUT> createFSM() {
    return new FSM<SUT>() {

      @Override
      public State<SUT> initialState() {
        return null;
      }

      @Override
      public State<SUT>[] states() {
        return Arrays.copyOf(SimpleFSMFactory.this.states, SimpleFSMFactory.this.states.length);
      }

      @Override
      public Action[] actions() {
        return new Action[0];
      }
    };
  }

  public static void main(String[] args) throws Exception {
    System.out.println(ExampleFSM.class.getMethod("values"));
  }
}
