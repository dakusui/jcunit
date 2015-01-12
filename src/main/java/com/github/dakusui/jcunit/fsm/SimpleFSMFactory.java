package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class SimpleFSMFactory<SUT> implements FSMFactory {
  private final List<State<SUT>>  states;
  private final State<SUT>        initialState;
  private       List<Action<SUT>> actions;

  public SimpleFSMFactory(Class<? extends Enum> clazz) {
    Checks.checknotnull(clazz);
    Checks.checktest(
        State.class.isAssignableFrom(clazz),
        "'%s' isn't a state (%s), the enum class given to this class must implement it.",
        clazz.getCanonicalName(), State.class.getCanonicalName()
    );

    this.states = Collections.unmodifiableList(getStatesFromFSMSpecClass(clazz));
    State<SUT> initial = chooseInitialState(clazz);
    Checks.checktest(
        initial != null,
        "No state is marked 'initial'. One and only one state must be annotated with '%s'",
        FSMSpec.Initial.class.getCanonicalName()
    );
    this.initialState = initial;

    this.actions = Collections.unmodifiableList(getActionsFromFSMSpecClass(clazz));
  }

  private List<Action<SUT>> getActionsFromFSMSpecClass(Class<? extends Enum> clazz) {
    List<Action<SUT>> ret = new LinkedList<Action<SUT>>();
    for (Method each : clazz.getMethods()) {
      if (each.isAnnotationPresent(FSMSpec.Transition.class)) {
        ret.add(createActionFromMethod(each));
      }
    }
    return ret;
  }

  private Action<SUT> createActionFromMethod(Method each) {
    return new Action<SUT>() {

      @Override
      public Object perform(SUT sut, Args args) throws Throwable {
        return null;
      }

      @Override
      public Object[] param(int i) {
        return new Object[0];
      }

      @Override
      public int numParams() {
        return 0;
      }
    };
  }

  private State<SUT> chooseInitialState(Class<? extends Enum> clazz) {
    State<SUT> initial = null;
    for (State<SUT> each : this.states) {
      try {
        String name = ((Enum<?>) each).name();
        Field f = clazz.getField(name);
        if (f.getAnnotation(FSMSpec.Initial.class) != null) {
          Checks.checktest(initial == null, "More than one state is marked 'initial', while one and only one state must be: %s", this.states);
          initial = each;
        }
      } catch (NoSuchFieldException e) {
        // This mustn't happen, since the name is coming from
        throw new JCUnitException(e.getMessage(), e);
      }
    }
    return initial;
  }

  private List<State<SUT>> getStatesFromFSMSpecClass(Class<? extends Enum> fsmSpecClass) {
    try {
      return Arrays.asList((State<SUT>[]) fsmSpecClass.getMethod("values").invoke(null));
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
        return SimpleFSMFactory.this.initialState;
      }

      @Override
      public List<State<SUT>> states() {
        return SimpleFSMFactory.this.states;
      }

      @Override
      public List<Action<SUT>> actions() {
        return SimpleFSMFactory.this.actions;
      }
    };
  }

  public static interface SimpleState<SUT> {
    boolean check(SUT sut);
  }
}
