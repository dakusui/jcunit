package com.github.dakusui.jcunit8.experiments.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import static com.github.dakusui.jcunit8.experiments.join.JoinExperimentUtils.loadPregeneratedOrGenerateAndSaveCoveringArrayFor;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.assertCoveringArray;
import static java.util.Objects.requireNonNull;

public class JoinExperiment {
  private final Builder spec;

  private JoinExperiment(JoinExperiment.Builder builder) {
    requireNonNull(builder);
    requireNonNull(builder.joinerFactory);
    requireNonNull(builder.lhsSpec);
    requireNonNull(builder.rhsSpec);
    this.spec = requireNonNull(builder);
  }

  public void exercise() {
    List<Tuple> joined = null;
    List<Tuple> lhs = loadOrGenerateCoveringArray(
        this.spec.lhsSpec,
        this.spec.lhsStrength.applyAsInt(this.spec.strength),
        this.spec.generator);
    List<Tuple> rhs = loadOrGenerateCoveringArray(
        this.spec.rhsSpec,
        this.spec.rhsStrength.applyAsInt(this.spec.strength),
        this.spec.generator);
    System.out.println(JoinReport.header());
    for (int i = 0; i < spec.times; i++) {
      CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
      joined = exerciseJoin(lhs, rhs, this.spec.strength, this.spec.joinerFactory);
      System.out.printf(
          "%s%n",
          new JoinReport(
              formatCoveringArray(lhs, this.spec.lhsStrength, this.spec.lhsSpec),
              formatCoveringArray(rhs, this.spec.rhsStrength, this.spec.rhsSpec),
              joined.size(),
              stopWatch.get()
          )
      );
    }
    if (spec.verification) {
      FactorSpace joinedFactorSpace = FactorSpace.create(
          new ArrayList<Factor>(spec.lhsSpec.numFactors() + spec.rhsSpec.numFactors()) {{
            addAll(spec.lhsSpec.build().getFactors());
            addAll(spec.rhsSpec.build().getFactors());
          }},
          Collections.emptyList()
      );
      assertCoveringArray(joined, joinedFactorSpace, spec.strength);
    }
  }

  public void joinAndPrint() {
    List<Tuple> lhs = loadOrGenerateCoveringArray(
        this.spec.lhsSpec,
        this.spec.lhsStrength.applyAsInt(this.spec.strength),
        this.spec.generator);
    List<Tuple> rhs = loadOrGenerateCoveringArray(
        this.spec.rhsSpec,
        this.spec.rhsStrength.applyAsInt(this.spec.strength),
        this.spec.generator);
    exerciseJoin(lhs, rhs, this.spec.strength, this.spec.joinerFactory).forEach(System.out::println);
  }

  private String formatCoveringArray(List<Tuple> ca, IntUnaryOperator p, FactorSpaceSpec p2) {
    return String.format("|CA(%s, %s)|=%s", p.applyAsInt(this.spec.strength), p2.signature(), ca.size());
  }

  private static List<Tuple> exerciseJoin(List<Tuple> lhs, List<Tuple> rhs, int strength, Function<Requirement, Joiner> joinerFactory) {
    return CoveringArrayGenerationUtils.join(
        lhs,
        rhs,
        joinerFactory,
        strength
    );
  }

  private static List<Tuple> loadOrGenerateCoveringArray(
      FactorSpaceSpec factorSpaceSpec,
      int strength,
      BiFunction<FactorSpace, Integer, List<Tuple>> generator) {
    return loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
        factorSpaceSpec,
        strength,
        generator
    );
  }

  public static class Builder implements Cloneable {
    Function<Requirement, Joiner>                 joinerFactory;
    FactorSpaceSpec                               lhsSpec;
    FactorSpaceSpec                               rhsSpec;
    BiFunction<FactorSpace, Integer, List<Tuple>> generator   = CoveringArrayGenerationUtils::generateWithIpoGplus;
    int                                           strength    = 2;
    IntUnaryOperator                              lhsStrength = i -> i;
    IntUnaryOperator                              rhsStrength = i -> i;
    int                                           times       = 1;
    private boolean verification;

    public Builder lhs(FactorSpaceSpec lhs) {
      return this.lhs(lhs, i -> i);
    }

    public Builder lhs(FactorSpaceSpec lhs, int strength) {
      return this.lhs(lhs, i -> strength);
    }

    public Builder lhs(FactorSpaceSpec lhs, IntUnaryOperator strength) {
      this.lhsSpec = lhs;
      this.lhsStrength = strength;
      return this;
    }

    public Builder rhs(FactorSpaceSpec rhs) {
      return this.rhs(rhs, i -> i);
    }

    public Builder rhs(FactorSpaceSpec rhs, int strength) {
      return this.rhs(rhs, i -> strength);
    }

    public Builder rhs(FactorSpaceSpec rhs, IntUnaryOperator strength) {
      this.rhsSpec = rhs;
      this.rhsStrength = strength;
      return this;
    }

    public Builder times(int times) {
      this.times = times;
      return this;
    }

    public Builder joiner(Function<Requirement, Joiner> joinerFactory) {
      this.joinerFactory = joinerFactory;
      return this;
    }

    public Builder generator(BiFunction<FactorSpace, Integer, List<Tuple>> generator) {
      this.generator = generator;
      return this;
    }

    public Builder strength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder verification(boolean enabled) {
      this.verification = enabled;
      return this;
    }

    public JoinExperiment build() {
      return new JoinExperiment(this.clone());
    }

    @Override
    public JoinExperiment.Builder clone() {
      try {
        return (JoinExperiment.Builder) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
