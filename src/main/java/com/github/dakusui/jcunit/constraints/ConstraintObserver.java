package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;

public interface ConstraintObserver {
  public void newConstraint(Tuple constraint);
}
