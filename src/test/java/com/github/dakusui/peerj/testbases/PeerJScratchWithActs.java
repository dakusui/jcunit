package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.utils.PeerJUtils2.baseDirFor;
import static com.github.dakusui.peerj.utils.PeerJUtils2.resultFile;
import static java.lang.String.format;

public class PeerJScratchWithActs extends PeerJScratch {
  public PeerJScratchWithActs(Spec spec) {
    super(spec);
  }

  @Test
  public void scratchGenerationWithActs() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "acts";
    String partitionerName = "none";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    StopWatch<PeerJScratch, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductScratchActsExperiment", (PeerJScratch self) ->
            generateWithActs(
                baseDir,
                factorSpace,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm(),
                constraintHandlingMethod())),
        (PeerJScratch self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }
}
