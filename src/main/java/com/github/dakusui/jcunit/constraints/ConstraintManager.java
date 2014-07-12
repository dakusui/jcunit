package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;

public interface ConstraintManager {
  public boolean check(Tuple tuple);

  void addObserver(ConstraintObserver observer);

  void removeObservers(ConstraintObserver observer);
}
