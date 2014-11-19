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
     */
    Object perform(SUT sut, Object[] args) throws Throwable;

    Factor[] params();

    ConstraintManager createConstraintManager();
}
