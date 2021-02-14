package com.github.dakusui.jcunitx.testsuite;

import com.github.dakusui.jcunitx.core.tuples.Tuple;

public interface Row extends Tuple {
  class Impl extends Tuple.Sorted implements Row {

  }

  static Row fromTuple(Tuple tuple) {
    Row ret = new Impl();
    ret.putAll(tuple);
    return ret;
  }
}
