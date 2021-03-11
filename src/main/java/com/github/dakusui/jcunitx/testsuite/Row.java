package com.github.dakusui.jcunitx.testsuite;

import com.github.dakusui.jcunitx.core.tuples.Tuple;
import com.github.dakusui.jcunitx.model.factor.FactorSpace;

public interface Row extends Tuple {
  FactorSpace schema();

  class Impl extends Tuple.Sorted implements Row {
    @Override
    public FactorSpace schema() {
      return null;
    }
  }

  static Row fromTuple(Tuple tuple) {
    Row ret = new Impl();
    ret.putAll(tuple);
    return ret;
  }
}
