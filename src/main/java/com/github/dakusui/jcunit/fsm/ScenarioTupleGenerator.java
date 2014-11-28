package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGeneratorBase;

class ScenarioTupleGenerator<SUT> extends TupleGeneratorBase {
  @Override
  public Tuple getTuple(int tupleId) {
    return null;
  }

  @Override
  protected long initializeTuples(Object[] params) {
    return 0;
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }
}
