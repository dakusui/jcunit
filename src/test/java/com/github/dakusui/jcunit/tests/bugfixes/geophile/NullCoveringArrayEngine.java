package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngineBase;

import java.util.Collections;
import java.util.List;

public class NullCoveringArrayEngine extends CoveringArrayEngineBase {
  @Override
  protected List<Tuple> generate() {
    return Collections.emptyList();
  }
}
