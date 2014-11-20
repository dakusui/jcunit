package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factor;

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
   *
   * {@code args} is composed from the returned value of {@code params} method.
   * The framework will pick up a value from a factor's levels returned by the method
   * one by one and creates an array of objects.
   *
   * The array will be passed to this method's second argument.
   */
  Object perform(SUT sut, Args args) throws Throwable;

  /**
   * Returns factors of this action.
   *
   * The returned factors have the levels each of which can be an argument for
   * {@code perform} method of this class.
   *
   * @see Action#perform(Object, com.github.dakusui.jcunit.fsm.Args)
   * @param builder A builder object by which this method is called.
   * @return Factors to be used to perform this action.
   */
  Factor[] params(ScenarioFactors.Builder builder);

  ConstraintManager createConstraintManager();
}
