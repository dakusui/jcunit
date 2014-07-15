package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ConstraintObserver {
  public void implicitConstraintFound(Tuple constraint);
}
