package com.github.dakusui.peerj;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.utils.CasaUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;

import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.restoreStdOutErr;
import static com.github.dakusui.jcunit8.testutils.UTUtils.TestUtils.suppressStdOutErrIfUnderPitestOrSurefire;
import static com.github.dakusui.peerj.acts.Acts.runActs;
import static com.github.dakusui.peerj.utils.CasaUtils.renameFactors;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;

public abstract class CasaExperimentBase {
  @Before
  public void before() {
    suppressStdOutErrIfUnderPitestOrSurefire();
  }

  @After
  public void after() {
    restoreStdOutErr();
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
    FORBIDDEN_TUPLES("forbiddentuples");

    private final String name;

    ConstraintHandlingMethod(String handlerName) {
      this.name = handlerName;
    }
  }

  protected List<Tuple> conductActsExperiment(CasaUtils def) {
    Requirement requirement = CasaUtils.requirement(strength());
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength()
    );
    return generateWithActs(
        CasaUtils.baseDirFor(def, "acts"),
        casaModel.factorSpace,
        casaModel.strength,
        algorithm(),
        constraintHandlingMethod())
        .stream()
        .peek(System.err::println)
        .collect(toList());
  }

  protected List<Tuple> conductJoinExperiment(CasaUtils def, Partitioner partitioner) {
    Requirement requirement = CasaUtils.requirement(strength());
    CasaUtils.CasaModel casaModel = CasaUtils.readCasaModel(
        def,
        "prefix",
        requirement.strength());
    return partitioner.apply(casaModel.factorSpace)
        .parallelStream()
        .peek(factorSpace -> System.err.println("->" + factorSpace))
        .peek(factorSpace -> {
          if (factorSpace.getFactorNames().isEmpty())
            throw new CasaUtils.NotCombinatorialJoinApplicable(def.toString());
        })
        .map(factorSpace -> generateWithActs(
            CasaUtils.baseDirFor(def, "join"),
            factorSpace,
            casaModel.strength,
            algorithm(),
            constraintHandlingMethod()))
        .map(arr -> arr.stream().map((Tuple t) -> renameFactors(t, currentThread().getId())).collect(toList()))
        .map(SchemafulTupleSet::fromTuples)
        .reduce(new Joiner.WeakenProduct(requirement))
        .orElseThrow(NoSuchElementException::new)
        .stream()
        .peek(System.err::println)
        .collect(toList());
  }

  abstract protected ConstraintHandlingMethod constraintHandlingMethod();

  abstract protected Algorithm algorithm();

  abstract protected int strength();

  public static List<Tuple> generateWithActs(File baseDir, FactorSpace factorSpace, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
    return runActs(baseDir, factorSpace, strength, algorithm.name, constraintHandlingMethod.name);
  }

  public static class Spec {
    final CasaUtils                def;
    final int                      strength;
    final Algorithm                algorithm;
    final ConstraintHandlingMethod constraintHandlingMethod;

    public Spec(CasaUtils def, int strength, Algorithm algorithm, ConstraintHandlingMethod constraintHandlingMethod) {
      this.def = def;
      this.strength = strength;
      this.algorithm = algorithm;
      this.constraintHandlingMethod = constraintHandlingMethod;
    }

    @Override
    public String toString() {
      return format("%s:t=%s:algorithm=%s:constraintHandling=%s", def, strength, algorithm, constraintHandlingMethod);
    }

    static Spec create(CasaUtils def) {
      return new Builder()
          .def(def)
          .strength(2)
          .algorithm(Algorithm.IPOG)
          .constraintHandlingMethod(ConstraintHandlingMethod.FORBIDDEN_TUPLES)
          .build();
    }

    public static class Builder {
      CasaUtils                def;
      int                      strength;
      Algorithm                algorithm;
      ConstraintHandlingMethod constraintHandlingMethod;

      public Builder def(CasaUtils def) {
        this.def = def;
        return this;
      }

      public Builder strength(int strength) {
        this.strength = strength;
        return this;
      }

      public Builder algorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        return this;
      }

      public Builder constraintHandlingMethod(ConstraintHandlingMethod constraintHandlingMethod) {
        this.constraintHandlingMethod = constraintHandlingMethod;
        return this;
      }

      public Spec build() {
        return new Spec(def, strength, algorithm, constraintHandlingMethod);
      }
    }
  }
}
