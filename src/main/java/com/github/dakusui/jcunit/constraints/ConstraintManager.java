package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.generators.ipo2.Tuple;

public interface ConstraintManager {
  public boolean check(Tuple cand);

  void addObserver(ConstraintObserver observer);

  void removeObservers(ConstraintObserver observer);
}
