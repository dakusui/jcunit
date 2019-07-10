package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;

public class NormalizedFactorSpace implements FactorSpace {
  private final FactorSpace rawFactorSpace;

  private NormalizedFactorSpace(FactorSpace rawFactorSpace) {
    this.rawFactorSpace = rawFactorSpace;
  }

  @Override
  public List<Constraint> getConstraints() {
    return null;
  }

  @Override
  public List<Factor> getFactors() {
    return null;
  }

  @Override
  public Factor getFactor(String name) {
    return null;
  }

  public String signature() {
    return null;
  }

  public FactorSpace rawFactorSpace() {
    return rawFactorSpace;
  }

  static NormalizedFactorSpace normalize(FactorSpace factorSpace) {
    return new NormalizedFactorSpace(factorSpace);
  }
}
