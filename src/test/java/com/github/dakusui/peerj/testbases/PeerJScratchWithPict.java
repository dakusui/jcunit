package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.utils.PeerJUtils2.*;
import static java.lang.String.format;

public class PeerJScratchWithPict extends PeerJBase {
  public PeerJScratchWithPict(Spec spec) {
    super(spec);
  }

  @Test(timeout = 600_000)
  public void scratchGenerationWithPict() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "pict";
    String partitionerName = "none";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    StopWatch<PeerJScratchWithPict, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductScratchPictExperiment", (PeerJScratchWithPict self) ->
            generateWithPict(baseDir, factorSpace, requirement.strength())),
        (PeerJScratchWithPict self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }
}
