package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

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

  <SPEC extends FsmSpec<SUT>> SPEC spec();

  class Base<SUT> implements State<SUT> {
    final         FsmSpec<SUT>            stateSpec;
    private final Map<String, Method>     actionMethods;
    private final Field                   stateSpecField;
    private final FiniteStateMachine<SUT> fsm;
    private final String                  fsmName;

    public Base(String fsmName, FiniteStateMachine<SUT> fsm, FsmSpec<SUT> stateSpec, Map<String, Method> actionMethods, Field stateSpecField) {
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

    @SuppressWarnings("unchecked")
    @Override
    public Expectation<SUT> expectation(Action<SUT> action, Args args) {
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
      return buildExpectation(m, args);
    }

    @SuppressWarnings("unchecked")
    private Expectation<SUT> buildExpectation(Method m, Args args) {
      Object[] argsToMethod = Utils.concatenate(
          new Object[] { new Expectation.Builder<>(this.fsmName, fsm) },
          args.values()
      );
      try {
        return (Expectation<SUT>) m.invoke(stateSpec, argsToMethod);
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
        throw Checks.fail();
      } catch (InvocationTargetException e) {
        throw Checks.wraptesterror(
            e,
            "Method '%s/%s' of '%s' must always succeed and return an object of '%s'.",
            m.getName(), args.values().length, stateSpec.getClass().getCanonicalName(), Expectation.class.getCanonicalName()
        );
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <SPEC extends FsmSpec<SUT>> SPEC spec() {
      return (SPEC) this.stateSpec;
    }

    @Override
    public int hashCode() {
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object another) {
      //noinspection SimplifiableIfStatement
      if (!(another instanceof State)) {
        return false;
      }
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
