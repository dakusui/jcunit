package com.github.dakusui.jcunit.generators.ipo2.constraintmanagers;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.generators.ipo2.Tuple;

public class NullConstraintManager extends ConstraintManagerBase {
  @Override public boolean check(Tuple tuple) {
    return true;
  }
}
