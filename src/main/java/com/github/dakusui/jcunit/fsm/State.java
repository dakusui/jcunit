package com.github.dakusui.jcunit.fsm;

/**
 * @param <SUT> A class of software under test.
 */
public interface State<SUT> {
  /**
   * Returns an {@code Expectation} when an {@code action} is performed with specified {@code args}
   * on an SUT in given state defined by this object.
   *
   * @param action An action to be performed.
   * @param args   Arguments with which {@code action} is performed.
   * @return An expectation.
   */
  Expectation expectation(Action action, Args args);

  /**
   * Checks if the given {@code sut} (software under test) satisfies the conditions
   * for it to be in the state that this object represents.
   *
   * @param sut An object that represents software under test.
   * @return true - {@code sut} satisfies this state / false - otherwise.
   */
  boolean matches(SUT sut);
}
