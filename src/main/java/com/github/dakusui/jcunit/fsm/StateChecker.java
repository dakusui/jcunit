package com.github.dakusui.jcunit.fsm;

public interface StateChecker<SUT> {
  /**
   * Checks if the given {@code sut} (software under test) satisfies the conditions
   * for it to be in the state that this object represents.
   *
   * @param sut An object that represents software under test.
   * @return true - {@code sut} satisfies this state / false - otherwise.
   */
  boolean check(SUT sut);
}
