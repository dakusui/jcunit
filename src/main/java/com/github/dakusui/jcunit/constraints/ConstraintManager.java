package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.factor.Factors;

public interface ConstraintManager {
  public boolean check(Tuple tuple);

  void setFactors(Factors factors);

  Factors getFactors();

  void addObserver(ConstraintObserver observer);

  void removeObservers(ConstraintObserver observer);
}
