package com.github.dakusui.jcunit.fsm;

/**
 * An interface that represents an action that can be performed on {@code SUT}.
 *
 * @param <SUT> A software under test.
 */
public interface Action<SUT> {
  public static final Action<?> VOID = new Action() {
    @Override
    public Object perform(Object o, Args args) throws Throwable {
      return FSMFactors.VOID;
    }

    @Override
    public Object[] parameterFactorLevels(int i) {
      return new Object[0];
    }

    @Override
    public int numParameterFactors() {
      return 0;
    }
  };

  /**
   * Performs this action with {@code args} on a given SUT {@code sut}.
   * An implementation of this method should usually represent and execute a method of
   * {@code sut} and return the value the method returns.
   * <p/>
   * {@code args} is composed from the returned value of {@code params} method.
   * The framework will pick up a value from a factor's levels returned by the method
   * one by one and creates an array of objects.
   * <p/>
   * The array will be passed to this method's second argument.
   */
  Object perform(SUT sut, Args args) throws Throwable;

  /**
   * Returns {@code i}th factor's levels.
   *
   * @param i a factor's index.
   */
  Object[] parameterFactorLevels(int i);

  /**
   * Returns a number of parameters that this action takes.
   */
  int numParameterFactors();
}
