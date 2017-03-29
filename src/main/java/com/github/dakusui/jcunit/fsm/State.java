package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @param <SUT> A class of software under test.
 */
public interface State<SUT> extends StateChecker<SUT>, Serializable {
  abstract class Void<SUT> implements State<SUT> {
    @SuppressWarnings("unchecked")
    public static <SUT> State<SUT> getInstance() {
      return (State<SUT>) INSTANCE;
    }

    private static Void INSTANCE = new Void() {
      public Expectation expectation(Action action, Args args) {
        /////
        // Since no action should be performed on VOID state, which represents  a state after
        // invalid operation is performed, only VOID action, which represents 'no action',
        // is only possible action.
        //
        // As of now, Action.VOID isn't introduced to design non-deterministic FSM.
        // non-deterministic FSM is not supported by JCUnit yet...
        if (action == Action.Void.getInstance() && args.size() == 0) {
          //noinspection unchecked
          return new Expectation(
              "(VOID)",
              Output.Type.VALUE_RETURNED,
              this,
              new OutputChecker.MatcherBased(Output.Type.VALUE_RETURNED, CoreMatchers.anything())
          );
        }
        return null;
      }

      @Override
      public boolean check(Object o) {
        ////
        // Once the FSM is given an invalid input (action and args), nothing
        // can be guaranteed.
        // Whatever happens on SUT, it's possible in terms of software specification and
        // since anything is possible, this method always return true regardless of SUT state.
        return true;
      }

      @Override
      public String toString() {
        return "(VOID)";
      }
    };
  }

  /**
   * Returns an {@code Expectation} when an {@code action} is performed with specified {@code args}
   * on an SUT in given state defined by this object.
   * <p/>
   * If {@code action} and {@code args} are not valid and shouldn't be tested (even as a negative-test)
   * on this state, {@code null} should be returned.
   * E.g., if an action should take a couple of integer arguments, but at least one of them must be non-zero
   * and at the same time either of them can take zero value.
   * In this case, by making this method return {@code null}, users can exclude test patterns those arguments
   * are set to zero at once.
   * <code>
   * Expectation expectation(Action action, Args args) {
   * ...
   * if (args.values()[0].equals(0) && args.values()[1].equals(0) return null;
   * ...
   * }
   * </code>
   *
   * @param action An action to be performed.
   * @param args   Arguments with which {@code action} is performed.
   * @return An expectation to be performed with SUT.
   */
  Expectation<SUT> expectation(Action<SUT> action, Args args);

  class Base<SUT> implements State<SUT> {
    final         FSMSpec<SUT>            stateSpec;
    private final Map<String, Method>     actionMethods;
    private final Field                   stateSpecField;
    private final FiniteStateMachine<SUT> fsm;
    private final String                  fsmName;

    public Base(String fsmName, FiniteStateMachine<SUT> fsm, FSMSpec<SUT> stateSpec, Map<String, Method> actionMethods, Field stateSpecField) {
      this.fsm = fsm;
      this.stateSpec = stateSpec;
      this.actionMethods = actionMethods;
      this.stateSpecField = stateSpecField;
      this.fsmName = fsmName;
    }

    @Override
    public boolean check(SUT sut) {
      return stateSpec.check(sut);
    }

    @Override
    public Expectation<SUT> expectation(Action<SUT> action, Args args) {
      Expectation<SUT> ret = null;
      Method m = Checks.checknotnull(actionMethods.get(action.id()), "Unknown action '%s' was given.", action);
      Checks.checktest(
          Expectation.class.isAssignableFrom(m.getReturnType()),
          "Method '%s/%s' of '%s' must return an '%s' object (but '%s' was returned).",
          m.getName(),
          m.getParameterTypes().length,
          m.getDeclaringClass().getCanonicalName(),
          Expectation.class.getCanonicalName(),
          m.getReturnType().getCanonicalName()
      );
      Object[] argsToMethod = Utils.concatenate(
          new Object[] { new Expectation.Builder<SUT>(this.fsmName, fsm) },
          args.values()
      );
      try {
        //noinspection unchecked
        ret = (Expectation<SUT>) m.invoke(stateSpec, argsToMethod);
      } catch (IllegalArgumentException e) {
        throw Checks.wraptesterror(
            e,
            "Wrong types: '%s/%s' of '%s' can't be executed with %s",
            m.getName(),
            m.getParameterTypes().length,
            m.getDeclaringClass(),
            Arrays.toString(argsToMethod)
        );
      } catch (IllegalAccessException e) {
        // Since the method is validated in advance, this path should never be executed.
        Checks.fail();
      } catch (InvocationTargetException e) {
        throw Checks.wraptesterror(
            e,
            "Method '%s/%s' of '%s' must always succeed and return an object of '%s'.",
            m.getName(), args.values().length, stateSpec.getClass().getCanonicalName(), Expectation.class.getCanonicalName()
        );
      }
      return ret;
    }

    @Override
    public int hashCode() {
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object another) {
      return this.toString().equals(another.toString());
    }

    @Override
    public String toString() {
      StateSpec ann = this.stateSpecField.getAnnotation(StateSpec.class);
      if (ann.value().length() > 0) {
        return String.format("%s(%s)", stateSpecField.getName(), ann.value());
      }
      return String.format("%s", stateSpecField.getName());
    }
  }
}
