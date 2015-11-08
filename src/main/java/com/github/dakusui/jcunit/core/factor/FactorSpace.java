package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;

import java.util.List;

public class FactorSpace {
  public final Factors    factors;
  public final Constraint constraint;

  public FactorSpace(Factors factors, Constraint constraint) {
    this.factors = factors;
    this.constraint = constraint;
  }

  public static class Builder implements CoreBuilder<FactorSpace> {

    private final List<FactorSource> descs;

    public Builder() {
      this.descs = Utils.newList();
    }

    public Builder add(FactorSource desc) {
      this.descs.add(desc);
      return this;
    }

    @Override
    public FactorSpace build() {
      Factors.Builder b = new Factors.Builder();
      for (FactorSource eachDesc : descs) {
        for (Factor eachFactor : eachDesc.createFactors()) {
          b.add(eachFactor);
        }
      }
      Factors factors = b.build();
      Constraint constraint = null;
      return new FactorSpace(
          factors,
          constraint
      );
    }
  }
}
