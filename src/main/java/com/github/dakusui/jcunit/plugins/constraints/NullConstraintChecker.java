package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.List;

public class NullConstraintChecker extends ConstraintChecker.Base {
  @Override
  public boolean check(Tuple tuple) {
    return true;
  }

  @Override
  public List<Constraint> getConstraints() {
    return Collections.emptyList();
  }
}
