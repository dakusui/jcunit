package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.ValueTuple;

/**
 * Created by hiroshi on 14/06/27.
 */
public interface ConstraintManager<T, U> {
  public boolean check(ValueTuple<String, Object> cand);

  void addObserver(ConstraintObserver<T, U> observer);

  void removeObservers(ConstraintObserver<T, U> observer);
}
