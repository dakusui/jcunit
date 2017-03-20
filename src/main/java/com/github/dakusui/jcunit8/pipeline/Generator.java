package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;

/**
 */
public interface Generator {
  List<Tuple> generate();

  interface Factory {
    Generator create(FactorSpace factorSpace, Requirement requirement);
  }
}
