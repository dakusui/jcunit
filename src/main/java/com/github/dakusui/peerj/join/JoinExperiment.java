package com.github.dakusui.peerj.join;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import static com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils.assertCoveringArray;
import static com.github.dakusui.peerj.utils.JoinExperimentUtils.loadPregeneratedOrGenerateAndSaveCoveringArrayFor;
import static com.github.dakusui.peerj.utils.JoinExperimentUtils.timeSpentForGeneratingCoveringArray;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

public class JoinExperiment implements Experiment {
  protected final Builder spec;

  protected JoinExperiment(JoinExperiment.Builder builder) {
    requireNonNull(builder);
    requireNonNull(builder.joinerFactory);
    requireNonNull(builder.lhsSpec);
    requireNonNull(builder.rhsSpec);
    this.spec = requireNonNull(builder);
  }

  public String formatCoveringArray(List<Tuple> ca, IntUnaryOperator p, FactorSpaceSpec p2) {
    return String.format("|CA(%s, %s)|=%s", p.applyAsInt(this.spec.strength), p2.createSignature(), ca.size());
  }

  public static List<Tuple> exerciseJoin(List<Tuple> lhs, List<Tuple> rhs, int strength, Function<Requirement, Joiner> joinerFactory) {
    return CoveringArrayGenerationUtils.join(
        lhs,
        rhs,
        joinerFactory,
        strength
    );
  }

  public static List<Tuple> loadOrGenerateCoveringArray(
      FactorSpaceSpec factorSpaceSpec,
      int strength,
      Generator generator) {
    return loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
        factorSpaceSpec,
        strength,
        generator
    );
  }

  @Override
  public Report conduct() {
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    try {
      long beforePreparation = System.currentTimeMillis();
      Future<Entry<List<Tuple>, Long>> lhsFuture = executorService.submit(() -> loadOrGenerateCoveringArrayWithGenerationTime(
          spec.lhsSpec,
          spec.lhsStrength.applyAsInt(spec.strength),
          spec.generator));
      Future<Entry<List<Tuple>, Long>> rhsFuture = executorService.submit(() -> loadOrGenerateCoveringArrayWithGenerationTime(
          spec.rhsSpec,
          spec.rhsStrength.applyAsInt(spec.strength),
          spec.generator));
      Entry<List<Tuple>, Long> lhs = futureGet(lhsFuture);
      Entry<List<Tuple>, Long> rhs = futureGet(rhsFuture);
      long afterPreparation = System.currentTimeMillis();
      System.out.println(JoinReport.header());
      CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
      List<Tuple> joined = exerciseJoin(lhs.getKey(), rhs.getKey(), this.spec.strength, this.spec.joinerFactory);
      return new JoinReport(
          formatCoveringArray(lhs.getKey(), this.spec.lhsStrength, this.spec.lhsSpec),
          formatCoveringArray(rhs.getKey(), this.spec.rhsStrength, this.spec.rhsSpec),
          joined.size(),
          stopWatch.get() + max(afterPreparation - beforePreparation, max(lhs.getValue(), rhs.getValue())));
    } finally {
      executorService.shutdownNow();
    }
  }

  private Entry<List<Tuple>, Long> futureGet(Future<Entry<List<Tuple>, Long>> lhsFuture) {
    try {
      return lhsFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private static Entry<List<Tuple>, Long> loadOrGenerateCoveringArrayWithGenerationTime(FactorSpaceSpec lhsSpec, int strength, Generator generator) {
    List<Tuple> tuples = loadOrGenerateCoveringArray(
        lhsSpec,
        strength,
        generator);
    long generationTime = timeSpentForGeneratingCoveringArray(
        lhsSpec,
        strength,
        generator);
    return entry(tuples, generationTime);
  }

  private static <K, V> Entry<K, V> entry(K k, V v) {
    return new Entry<K, V>() {
      @Override
      public K getKey() {
        return k;
      }

      @Override
      public V getValue() {
        return v;
      }

      @Override
      public V setValue(Object value) {
        throw new UnsupportedOperationException();
      }
    };
  }

  public static class Builder implements Cloneable {
    public Function<Requirement, Joiner> joinerFactory;
    public FactorSpaceSpec               lhsSpec;
    public FactorSpaceSpec rhsSpec;
    public Generator       generator   = CoveringArrayGenerationUtils::generateWithIpoGplus;
    public int              strength    = 2;
    public IntUnaryOperator lhsStrength = i -> i;
    public  IntUnaryOperator rhsStrength = i -> i;
    public int     times       = 1;
    public boolean verification;

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

    public Builder generator(Generator generator) {
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

  public interface Generator {
    List<Tuple> generate(File baseDir, FactorSpace factorSpace, Integer strength);
  }
}
