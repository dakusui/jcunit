package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ConstraintObserver {
  public void implicitConstraintFound(Tuple constraint);
}
