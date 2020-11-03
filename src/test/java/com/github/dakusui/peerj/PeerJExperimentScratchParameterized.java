package com.github.dakusui.peerj;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class PeerJExperimentScratchParameterized extends PeerJExperimentParameterized {

  public final  Spec        spec;
  private final FactorSpace factorSpace;
  private final String      dataSetName;

  public PeerJExperimentScratchParameterized(Spec spec) {
    this.spec = spec;
    this.factorSpace = spec.factorSpace;
    this.dataSetName = spec.factorSpaceName;
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
    return this.factorSpace;
  }

  protected String dataSetName() {
    return this.dataSetName;
  }

  @Test
  public void scratchGenerationWithActs() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "acts";
    String partitionerName = "none";
    File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    StopWatch<PeerJExperimentScratchParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductActsExperiment", (PeerJExperimentScratchParameterized self) ->
            generateWithActs(
                baseDir,
                factorSpace,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm(),
                constraintHandlingMethod())),
        (PeerJExperimentScratchParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void scratchGenerationWithWeakenProductCombinatorialJoin() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join";
    Partitioner partitioner = evenPartitioner();
    File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, partitioner.name());
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    StopWatch<PeerJExperimentScratchParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductJoinExperiment", (PeerJExperimentScratchParameterized self) -> generateWithCombinatorialJoin(requirement, baseDir, partitioner, factorSpace, algorithm(), constraintHandlingMethod(), "")),
        (PeerJExperimentScratchParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile(dataSetName, strength(), generationMode, partitioner.name()), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void incrementalGenerationWithActs() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "acts";
    String partitionerName = "incremental";
    File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithActs(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength, algorithm(), constraintHandlingMethod()));
    StopWatch<PeerJExperimentScratchParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalActsExperiment", (PeerJExperimentScratchParameterized self) ->
            extendWithActs(
                baseDir,
                factorSpace,
                base,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm(),
                constraintHandlingMethod())),
        (PeerJExperimentScratchParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void incrementalGenerationWithWeakenProductCombinatorialJoin() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join";
    String suffix = "incremental";
    File baseDir = baseDirFor(dataSetName, this.spec.strength, generationMode, suffix);
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithActs(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength, algorithm(), constraintHandlingMethod()));
    StopWatch<PeerJExperimentScratchParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalJoinExperiment", (PeerJExperimentScratchParameterized self) -> extendWithCombinatorialJoin(requirement, baseDir, factorSpace, base, algorithm(), constraintHandlingMethod())),
        (PeerJExperimentScratchParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      writeTo(resultFile(dataSetName, strength(), generationMode, suffix), Stream.of(stopWatch.report()));
    }
  }

  private static FactorSpace baseFactorSpaceFrom(FactorSpace factorSpace) {
    int baseDegree = factorSpace.getFactors().size() - 10;
    List<Factor> baseFactors = factorSpace.getFactors().subList(0, baseDegree);
    Set<String> baseFactorNames = baseFactors.stream().map(Factor::getName).collect(toSet());
    return FactorSpace.create(
        baseFactors,
        factorSpace.getConstraints()
            .stream()
            .filter(each -> each.involvedKeys().stream().anyMatch(baseFactorNames::contains))
            .peek(crossingConstraintFound(baseFactorNames))
            .collect(toList()));
  }
}
