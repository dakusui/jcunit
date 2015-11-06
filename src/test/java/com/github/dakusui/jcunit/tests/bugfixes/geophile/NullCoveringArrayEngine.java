package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;

import java.util.Collections;
import java.util.List;

public class NullCoveringArrayEngine extends CoveringArrayEngine.Base {
  @Override
  protected List<Tuple> generate() {
    return Collections.emptyList();
  }
}
