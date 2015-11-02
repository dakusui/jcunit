package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngineBase;

public class NullCoveringArrayEngine extends CoveringArrayEngineBase {
  @Override
  protected long initializeTuples() {
    return 0;
  }

  @Override
  protected Tuple getTuple(int tupleId) {
    throw new ArrayIndexOutOfBoundsException();
  }
}
