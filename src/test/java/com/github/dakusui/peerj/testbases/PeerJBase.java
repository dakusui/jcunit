package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@RunWith(Parameterized.class)
public abstract class PeerJBase extends ExperimentBase {
  public final Spec spec;

  public PeerJBase(Spec spec) {
    this.spec = spec;
  }

  public static List<Spec> parametersWith(int strength, ConstraintHandlingMethod constraintHandlingMethod, int begin, int end) {
    return parametersWith(strength, -1, constraintHandlingMethod, begin, end);
  }

  public static List<Spec> parametersWith(int baseStrength, int relationStrength, ConstraintHandlingMethod constraintHandlingMethod, int begin, int end) {
    int startInclusive = begin / 20;
    int endExclusive = end / 20;
    return IntStream.range(startInclusive, endExclusive)
        .map(i -> i * 20)
        .boxed()
        .flatMap(i -> Arrays.stream(ConstraintSet.values())
            .map(constraintSet -> new Spec.Builder()
                .strength(baseStrength)
                .degree(i)
                .rank(4)
                .constraintSet(constraintSet)
                .constraintHandlingMethod(constraintHandlingMethod)
                .relationStrength(relationStrength)
                .build()))
        .collect(toList());
  }

  @Override
  protected ConstraintHandlingMethod constraintHandlingMethod() {
    return spec.constraintHandlingMethod;
  }

  @Override
  protected Algorithm algorithm() {
    return spec.algorithm;
  }

  @Override
  protected int strength() {
    return spec.strength;
  }

  protected FactorSpace factorSpace() {
    return this.spec.factorSpace;
  }

  protected String dataSetName() {
    return this.spec.factorSpaceName;
  }

  public static class Spec extends ExperimentBase.Spec {
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

    public static class Builder extends ExperimentBase.Spec.Builder<PeerJBase.Spec.Builder> {
      private int           degree;
      private int           rank;
      private ConstraintSet constraintSet;
      private String        prefix;
      private int           relationStrength = -1;

      public Builder() {
        this.prefix("prefix").degree(100).rank(2).constraintSet(ConstraintSet.BASIC);
      }

      public PeerJBase.Spec.Builder degree(int degree) {
        this.degree = degree;
        return this;
      }

      public PeerJBase.Spec.Builder rank(int rank) {
        this.rank = rank;
        return this;
      }

      /**
       * Used for testing VSCA generation
       *
       * @param relationStrength Strength for higher strength than base one and it is used for internal covering arrays joined by base one.
       * @return This object.
       */
      public PeerJBase.Spec.Builder relationStrength(int relationStrength) {
        this.relationStrength = relationStrength;
        return this;
      }

      public PeerJBase.Spec.Builder constraintSet(ConstraintSet constraintSet) {
        this.constraintSet = requireNonNull(constraintSet);
        return this;
      }

      public PeerJBase.Spec.Builder prefix(String prefix) {
        this.prefix = prefix;
        return this;
      }

      @Override
      public PeerJBase.Spec build() {
        FactorSpaceSpec factorySpaceSpec = PeerJUtils.createFactorySpaceSpec(this.constraintSet, this.prefix, this.degree, this.strength, this.relationStrength);
        for (int i = 0; i < this.degree; i++)
          factorySpaceSpec.addFactor(this.rank);
        return new PeerJBase.Spec(
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
