package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.restoreStdOutErr;
import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire;
import static com.github.dakusui.peerj.PeerJUtils2.renameFactors;
import static com.github.dakusui.peerj.ext.ExternalEngine.GenerationMode.INCREMENTAL;
import static com.github.dakusui.peerj.ext.ExternalEngine.GenerationMode.SCRATCH;
import static com.github.dakusui.peerj.ext.acts.Acts.runActs;
import static com.github.dakusui.peerj.testbases.PeerJExperimentBase.ConstraintHandlingMethod.FORBIDDEN_TUPLES;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class PeerJExperimentBase {
  public abstract static class Spec {
    public final int                      strength;
    public final Algorithm                algorithm;
    public final ConstraintHandlingMethod constraintHandlingMethod;

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
      protected int                      strength;
      protected Algorithm                algorithm;
      protected ConstraintHandlingMethod constraintHandlingMethod;

      public Builder() {
        this.strength(2).algorithm(Algorithm.IPOG).constraintHandlingMethod(FORBIDDEN_TUPLES);
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
    return runActs(baseDir, factorSpace, strength, algorithm.name, SCRATCH, constraintHandlingMethod.name);
  }

  public static List<Tuple> generateWithCombinatorialJoin(Requirement requirement, File baseDir, Partitioner partitioner, FactorSpace factorSpace, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod, String messageOnFailure) {
    List<FactorSpace> factorSpaces = partitioner.apply(factorSpace);
    return factorSpaces
        .parallelStream()
        .peek(factorSpace1 -> System.err.println("->" + factorSpace1))
        .peek(factorSpace1 -> {
          if (factorSpace1.getFactorNames().isEmpty()) {
            throw new CasaDataSet.NotCombinatorialJoinApplicable(messageOnFailure);
          }
        })
        .map(factorSpace1 -> PeerJExperimentBase.generateWithActs(
            baseDir,
            FactorSpace.create(factorSpace1.getFactors(), factorSpace1.getConstraints()),
            factorSpace1.relationStrength() >= 0
                ? factorSpace1.relationStrength()
                : requirement.strength(),
            algorithm,
            constraintHandlingMethod))
        .map((List<Tuple> tuples) -> renameFactorsInTuples(tuples, currentThread().getId()))
        .map(SchemafulTupleSet::fromTuples)
        .reduce(new Joiner.WeakenProduct(requirement))
        .orElseThrow(NoSuchElementException::new);
  }

  public static List<Tuple> extendWithActs(File baseDir, FactorSpace factorSpace, SchemafulTupleSet base, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
    return runActs(baseDir, factorSpace, strength, algorithm.name, INCREMENTAL, constraintHandlingMethod.name, base);
  }

  public static SchemafulTupleSet extendWithCombinatorialJoin(Requirement requirement, File baseDir, FactorSpace factorSpace, SchemafulTupleSet base, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
    List<Factor> additionFactors = factorSpace.getFactors().subList(base.getAttributeNames().size(), factorSpace.getFactors().size());
    Set<String> additionFactorNames = additionFactors.stream()
        .map(Factor::getName)
        .collect(toSet());
    List<Constraint> additionConstraints = factorSpace.getConstraints().stream()
        .filter(each -> each.involvedKeys()
            .stream()
            .anyMatch(additionFactorNames::contains))
        .peek(crossingConstraintFound(additionFactorNames))
        .collect(toList());
    FactorSpace additionFactorSpace = FactorSpace.create(additionFactors, additionConstraints);
    return Stream.of(
        SchemafulTupleSet.fromTuples(renameFactorsInTuples(base, 1)),
        SchemafulTupleSet.fromTuples(
            renameFactorsInTuples(
                generateWithActs(
                    new File(baseDir, "addition"),
                    additionFactorSpace,
                    requirement.strength(),
                    algorithm,
                    constraintHandlingMethod), 2)))
        .reduce(new Joiner.WeakenProduct(requirement))
        .orElseThrow(NoSuchElementException::new);
  }

  public static Consumer<Constraint> crossingConstraintFound(Set<String> factorNames) {
    return each -> {
      if (!factorNames.containsAll(each.involvedKeys()))
        throw new RuntimeException("Crossing constraint is not supported");
    };
  }

  private static List<Tuple> renameFactorsInTuples(List<Tuple> tuples, long partitionId) {
    return tuples.stream().map((Tuple t) -> renameFactors(t, partitionId)).collect(toList());
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
