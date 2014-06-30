package com.github.dakusui.jcunit.generators.ipo2.constraintmanagers;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.ValueTuple;

/**
 * Created by hiroshi on 6/30/14.
 */
public class NullConstraintManager<T, U> extends ConstraintManagerBase<T, U> {
  @Override public boolean check(ValueTuple<String, Object> cand) {
    return true;
  }
}
