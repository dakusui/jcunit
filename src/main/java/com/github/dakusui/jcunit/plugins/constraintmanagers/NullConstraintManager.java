package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public class NullConstraintManager extends ConstraintManagerBase {
  @Override
  public boolean check(Tuple tuple) {
    return true;
  }
}
