package com.github.dakusui.jcunit.fsm;

import org.hamcrest.CoreMatchers;

/**
 * @param <SUT> A class of software under test.
 */
public interface State<SUT> extends StateChecker<SUT> {
  /**
   * When an invalid operation is performed, JCUnit
   */
  public static final State<?> VOID = new State() {
    @Override
    public Expectation<?> expectation(Action action, Args args) {
      /////
      // Since no action should be performed on VOID state, which represents  a state after
      // invalid operation is performed, only VOID action, which represents 'no action',
      // is only possible action.
      //
      // As of now, Action.VOID isn't introduced to design non-deterministic FSM.
      // non-deterministic FSM is not supported by JCUnit yet...
      if (action == Action.VOID && args.size() == 0)
        return new Expectation(this, CoreMatchers.anything());
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
  };

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
   *
   * @param action An action to be performed.
   * @param args   Arguments with which {@code action} is performed.
   * @return An expectation.
   * @Override Expectation expectation(Action action, Args args) {
   * ...
   * if (args.values()[0].equals(0) && args.values()[1].equals(0) return null;
   * ...
   * }
   * </code>
   */
  Expectation<SUT> expectation(Action<SUT> action, Args args);
}
