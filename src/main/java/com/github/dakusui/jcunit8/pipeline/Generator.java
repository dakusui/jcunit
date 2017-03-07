package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.model.factorspace.FactorSpace;

import java.util.List;

/**
 */
public interface Generator {
  boolean supports(FactorSpace.Characteristics characteristics, Requirement requirement);

  double estimateTime(FactorSpace.Characteristics characteristics, Requirement requirement);

  double estimateSize(FactorSpace.Characteristics characteristics, Requirement requirement);

  List<Tuple> generate(FactorSpace factorSpace);
}
