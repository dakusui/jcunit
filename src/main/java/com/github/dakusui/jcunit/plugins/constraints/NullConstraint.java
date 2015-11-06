package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public class NullConstraint extends ConstraintBase {
  public NullConstraint() {
  }

  @Override
  public boolean check(Tuple tuple) {
    return true;
  }
}
