package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public class NullConstraintChecker extends ConstraintChecker.Base {
  @Override
  public boolean check(Tuple tuple) {
    return true;
  }
}
