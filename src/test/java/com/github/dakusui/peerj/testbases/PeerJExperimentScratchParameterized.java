package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.peerj.ext.shared.IoUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;

public abstract class PeerJExperimentScratchParameterized extends PeerJExperimentParameterized {

  public PeerJExperimentScratchParameterized(Spec spec) {
    super(spec);
  }

  @Before
  public void before() {
    Utils.invalidateMemos();
  }

  @Test
  public void scratchGenerationWithActs() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "acts";
    String partitionerName = "none";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitionerName);
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
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  @Test
  public void scratchGenerationWithWeakenProductCombinatorialJoin() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join";
    Partitioner partitioner = evenPartitioner();
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitioner.name());
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    StopWatch<PeerJExperimentScratchParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductJoinExperiment", (PeerJExperimentScratchParameterized self) -> generateWithCombinatorialJoin(requirement, baseDir, partitioner, factorSpace, algorithm(), constraintHandlingMethod(), "")),
        (PeerJExperimentScratchParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitioner.name()), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }
}
