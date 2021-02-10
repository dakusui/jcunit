package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;

public class PeerJScratchWithWPJoinBasedOnPict extends PeerJScratch {
  public PeerJScratchWithWPJoinBasedOnPict(Spec spec) {
    super(spec);
  }

  @Test
  public void scratchGenerationWithWeakenProductCombinatorialJoinBasedOnPict() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join-pict";
    Partitioner partitioner = evenPartitioner();
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitioner.name());
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    StopWatch<PeerJScratchWithWPJoinBasedOnPict, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductScratchJoinExperiment(PICT)", (PeerJScratchWithWPJoinBasedOnPict self) -> generateWithCombinatorialJoinBasedOnPict(requirement, baseDir, partitioner, factorSpace, "")),
        (PeerJScratchWithWPJoinBasedOnPict self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitioner.name()), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }
}
