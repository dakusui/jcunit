package com.github.dakusui.jcunit.experimentals.fsm;

/**
 *
 *
 * @param <SUT> A class of software under test.
 */
public interface State<SUT> {
  Expectation expectation(Action action, Args args);

  /**
   * Checks if the given {@code sut} (software under test) satisfies the conditions
   * for it to be in the state that this object represents.
   *
   * @param sut  An object that represents software under test.
   * @return true - {@code sut} satisfies this state / false - otherwise.
   */
  boolean matches(SUT sut);


}
