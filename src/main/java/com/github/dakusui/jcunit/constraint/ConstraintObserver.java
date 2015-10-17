package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ConstraintObserver {
  void implicitConstraintFound(Tuple constraint);
}
