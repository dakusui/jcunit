package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.baseDirFor;
import static com.github.dakusui.peerj.PeerJUtils2.resultFile;
import static java.lang.String.format;

public class PeerJIncrementalWithActs extends PeerJIncremental {
  public PeerJIncrementalWithActs(Spec spec) {
    super(spec);
  }

  @Test
  public void incrementalGenerationWithActs() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "acts";
    String partitionerName = "incremental";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithActs(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength, algorithm(), constraintHandlingMethod()));
    StopWatch<PeerJIncremental, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalActsExperiment", (PeerJIncremental self) ->
            extendWithActs(
                baseDir,
                factorSpace,
                base,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm(),
                constraintHandlingMethod())),
        (PeerJIncremental self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }
}
