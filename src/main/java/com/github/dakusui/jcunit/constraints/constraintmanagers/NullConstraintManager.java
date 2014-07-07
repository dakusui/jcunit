package com.github.dakusui.jcunit.constraints.constraintmanagers;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Tuple;

public class NullConstraintManager extends ConstraintManagerBase {
  @Override public boolean check(Tuple tuple) {
    return true;
  }
}
