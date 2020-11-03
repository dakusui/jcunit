package com.github.dakusui.peerj;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@RunWith(Parameterized.class)
public abstract class PeerJExperimentParameterized extends PeerJExperimentBase {
  public static class Spec extends PeerJExperimentBase.Spec {
    final String      factorSpaceName;
    final FactorSpace factorSpace;

    public Spec(String factorSpaceName, FactorSpace factorSpace, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
      super(strength, algorithm, constraintHandlingMethod);
      this.factorSpace = factorSpace;
      this.factorSpaceName = factorSpaceName;
    }

    @Override
    public String toString() {
      return format("%s:%s", this.factorSpaceName, super.toString());
    }

    public static class Builder extends PeerJExperimentBase.Spec.Builder<PeerJExperimentParameterized.Spec.Builder> {
      private int           degree;
      private int           rank;
      private ConstraintSet constraintSet;
      private String        prefix;
      private int           relationStrength = -1;

      public Builder() {
        this.prefix("prefix").degree(100).rank(2).constraintSet(ConstraintSet.BASIC);
      }

      public PeerJExperimentParameterized.Spec.Builder degree(int degree) {
        this.degree = degree;
        return this;
      }

      public PeerJExperimentParameterized.Spec.Builder rank(int rank) {
        this.rank = rank;
        return this;
      }

      /**
       * Used for testing VSCA generation
       *
       * @param relationStrength Strength for higher strength than base one and it is used for internal covering arrays joined by base one.
       * @return This object.
       */
      public PeerJExperimentParameterized.Spec.Builder relationStrength(int relationStrength) {
        this.relationStrength = relationStrength;
        return this;
      }

      public PeerJExperimentParameterized.Spec.Builder constraintSet(ConstraintSet constraintSet) {
        this.constraintSet = requireNonNull(constraintSet);
        return this;
      }

      public PeerJExperimentParameterized.Spec.Builder prefix(String prefix) {
        this.prefix = prefix;
        return this;
      }

      @Override
      public PeerJExperimentParameterized.Spec build() {
        FactorSpaceSpec factorySpaceSpec = PeerJUtils.createFactorySpaceSpec(this.constraintSet, this.prefix, this.degree, this.strength, this.relationStrength);
        for (int i = 0; i < this.degree; i++)
          factorySpaceSpec.addFactor(this.rank);
        return new PeerJExperimentParameterized.Spec(
            factorySpaceSpec.createSignature(),
            factorySpaceSpec.toFactorSpace(),
            this.strength,
            this.algorithm,
            this.constraintHandlingMethod
        );
      }
    }
  }
}
