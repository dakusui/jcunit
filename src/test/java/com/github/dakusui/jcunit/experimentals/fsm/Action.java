package com.github.dakusui.jcunit.experimentals.fsm;

/**
 * An interface that represents an action that can be performed on {@code SUT}.
 *
 * @param <SUT> A software under test.
 */
public interface Action<SUT> {
  /**
   * Performs this action with {@code args} on a given SUT {@code sut}.
   * An implementation of this method should usually represent and execute a method of
   * {@code sut} and return the value the method returns.
   */
  Object perform(SUT sut, Args args) throws Throwable;

  /**
   * Returns an array of {@code Args} objects each of which can be given to {@code perform} method
   * or {@code State#expect} method.
   *
   * @return An array of {@code Args} valid for using with this action.
   */
  Args[] args();

}
