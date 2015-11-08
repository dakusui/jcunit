package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;

import java.util.Collections;
import java.util.List;

public class NullCoveringArrayEngine extends CoveringArrayEngine.Base {
  @Override
  protected List<Tuple> generate(Factors factors, Constraint constraint) {
    return Collections.emptyList();
  }
}
