package com.github.dakusui.jcunit8.extras.abstracter;

import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum Abstractor {
  ;

  static FactorSpace toFactorSpace(FactorSpaceSpecForExperiments factorSpaceSpec) {
    return null;
  }

  static FactorSpace encode(FactorSpace factorSpace) {
    FactorSpaceSpecForExperiments ret = new FactorSpaceSpecForExperiments();
    List<Function<String, Integer>> factorNameEncoders = new ArrayList<>(factorSpace.getFactors().size());
    factorSpace.getFactors().forEach(f -> ret.addFactor(f.getLevels().size()));
    return ret.build();
  }

}
