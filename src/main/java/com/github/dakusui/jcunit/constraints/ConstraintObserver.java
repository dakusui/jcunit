package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.generators.ipo2.Tuple;

public interface ConstraintObserver {
  public void newConstraint(Tuple constraint);
}
