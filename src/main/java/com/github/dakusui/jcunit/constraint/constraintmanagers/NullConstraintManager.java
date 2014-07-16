package com.github.dakusui.jcunit.constraint.constraintmanagers;

import com.github.dakusui.jcunit.constraint.Violation;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.List;

public class NullConstraintManager extends ConstraintManagerBase {
  @Override public boolean check(Tuple tuple) {
    return true;
  }
}
