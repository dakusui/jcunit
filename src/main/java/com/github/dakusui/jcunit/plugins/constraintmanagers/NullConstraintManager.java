package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public class NullConstraintManager extends ConstraintManagerBase {
  public NullConstraintManager(Param.Translator<Object> translator) {
    super(translator);
  }

  @Override
  public boolean check(Tuple tuple) {
    return true;
  }
}
