package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.factor.Factors;

public interface ConstraintManager {
  void setFactors(Factors factors);

  void init(Object[] params);

  boolean check(Tuple tuple);

  Factors getFactors();

  void addObserver(ConstraintObserver observer);

  void removeObservers(ConstraintObserver observer);
}
