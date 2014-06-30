package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.ValueTuple;

/**
 * Created by hiroshi on 6/28/14.
 */
public interface ConstraintObserver<T, U> {
  public void newConstraint(ValueTuple<T, U> constraint);
}
