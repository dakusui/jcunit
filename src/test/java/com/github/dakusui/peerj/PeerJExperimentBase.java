package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.utils.CasaDataSet;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.restoreStdOutErr;
import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire;
import static com.github.dakusui.peerj.PeerJExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.PeerJExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static com.github.dakusui.peerj.PeerJUtils2.renameFactors;
import static com.github.dakusui.peerj.acts.Acts.runActs;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;

public abstract class PeerJExperimentBase {
  public abstract static class Spec {
    final int                      strength;
    final Algorithm                algorithm;
    final ConstraintHandlingMethod constraintHandlingMethod;

    public Spec(int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
      this.strength = strength;
      this.algorithm = algorithm;
      this.constraintHandlingMethod = constraintHandlingMethod;
    }

    @Override
    public String toString() {
      return format("t=%s:algorithm=%s:constraintHandling=%s", strength, algorithm, constraintHandlingMethod);
    }

    public abstract static class Builder<B extends Builder<B>> {
      int                      strength;
      Algorithm                algorithm;
      ConstraintHandlingMethod constraintHandlingMethod;

      public Builder() {
        this.strength(2).algorithm(IPOG).constraintHandlingMethod(FORBIDDEN_TUPLES);
      }

      @SuppressWarnings("unchecked")
      public B strength(int strength) {
        this.strength = strength;
        return (B) this;
      }

      @SuppressWarnings("unchecked")
      public B algorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        return (B) this;
      }

      @SuppressWarnings("unchecked")
      public B constraintHandlingMethod(ConstraintHandlingMethod constraintHandlingMethod) {
        this.constraintHandlingMethod = constraintHandlingMethod;
        return (B) this;
      }

      public abstract Spec build();
    }
  }

  @Before
  public void before() {
    suppressStdOutErrIfUnderPitestOrSurefire();
  }

  @After
  public void after() {
    restoreStdOutErr();
  }

  abstract protected ConstraintHandlingMethod constraintHandlingMethod();

  abstract protected Algorithm algorithm();

  abstract protected int strength();

  public static List<Tuple> generateWithActs(File baseDir, FactorSpace factorSpace, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
    return runActs(baseDir, factorSpace, strength, algorithm.name, constraintHandlingMethod.name);
  }

  public static List<Tuple> generateWithCombinatorialJoin(Requirement requirement, File baseDir, Partitioner partitioner, FactorSpace factorSpace, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod, String messageOnFailure) {
    List<FactorSpace> factorSpaces = partitioner.apply(factorSpace);
    return generateWithCombinatorialJoin(requirement, baseDir, factorSpaces, algorithm, constraintHandlingMethod, messageOnFailure);
  }

  private static SchemafulTupleSet generateWithCombinatorialJoin(Requirement requirement, File baseDir, List<FactorSpace> factorSpaces, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod, String messageOnFailure) {
    return factorSpaces
        .parallelStream()
        .peek(factorSpace -> System.err.println("->" + factorSpace))
        .peek(factorSpace -> {
          if (factorSpace.getFactorNames().isEmpty()) {
            throw new CasaDataSet.NotCombinatorialJoinApplicable(messageOnFailure);
          }
        })
        .map(factorSpace -> PeerJExperimentBase.generateWithActs(
            baseDir,
            FactorSpace.create(factorSpace.getFactors(), factorSpace.getConstraints()),
            factorSpace.relationStrength() >= 0
                ? factorSpace.relationStrength()
                : requirement.strength(),
            algorithm,
            constraintHandlingMethod))
        .map(arr -> arr.stream().map((Tuple t) -> renameFactors(t, currentThread().getId())).collect(toList()))
        .map(SchemafulTupleSet::fromTuples)
        .reduce(new Joiner.WeakenProduct(requirement))
        .orElseThrow(NoSuchElementException::new);
  }

  public enum Algorithm {
    IPOG("ipog"),
    ;

    public final String name;

    Algorithm(String algorithmName) {
      this.name = algorithmName;
    }
  }

  public enum ConstraintHandlingMethod {
    FORBIDDEN_TUPLES("forbiddentuples"),
    SOLVER("solver");

    private final String name;

    ConstraintHandlingMethod(String handlerName) {
      this.name = handlerName;
    }
  }

}
