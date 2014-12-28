package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.fsm.simplefsmexample.ExampleFSM;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
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
  public static @interface Initial {

  }

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Parameter {
    /**
     * Must return a name of public static method which returns an array whose
     * type is exactly the same as the parameter to which this annotation is attached.
     */
    public String value();
  }

  public static interface SimpleState<SUT> {
    boolean check(SUT sut);
  }

  private final State<SUT>[] states;
  private final Class<? extends Enum> fsmSpecClass;
  private final State<SUT> initialState;

  public SimpleFSMFactory(Class<? extends Enum> clazz) {
    Checks.checknotnull(clazz);
    Checks.checktest(
        State.class.isAssignableFrom(clazz),
        "'%s' isn't a state (%s), the enum class given to this class must implement it.",
        clazz.getCanonicalName(), State.class.getCanonicalName()
    );
    this.fsmSpecClass = clazz;
    try {
      this.states = (State<SUT>[])fsmSpecClass.getMethod("values").invoke(null);
    } catch (IllegalAccessException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      throw new JCUnitException(e.getMessage(), e);
    }

    State<SUT> initial = null;
    for (State<SUT> each : this.states) {
      try {
        String name = ((Enum<?>)each).name();
        Field f = this.fsmSpecClass.getField(name);
        if (f.getAnnotation(Initial.class) != null) {
          Checks.checktest(initial == null, "More than one state is marked 'initial', while one and only one state must be: %s", Arrays.toString(this.states));
          initial = each;
        }
      } catch (NoSuchFieldException e) {
        // This mustn't happen, since the name is coming from
        throw new JCUnitException(e.getMessage(), e);
      }
    }
    this.initialState = initial;
  }

  @Override
  public FSM<SUT> createFSM() {
    return new FSM<SUT>() {

      @Override
      public State<SUT> initialState() {
        return SimpleFSMFactory.this.initialState;
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
