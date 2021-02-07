package com.github.dakusui.peerj.ut.runners;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.testbases.PeerJExperimentParameterized;
import com.github.dakusui.peerj.testbases.StopWatch;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.baseDirFor;
import static com.github.dakusui.peerj.PeerJUtils2.resultFile;
import static com.github.dakusui.peerj.ut.runners.PeerJExperimentIncrementalParameterized.baseFactorSpaceFrom;
import static java.lang.String.format;

public class PeerjExperimentIncrementalParameterizedWithPict extends PeerJExperimentParameterized {
  public PeerjExperimentIncrementalParameterizedWithPict(Spec spec) {
    super(spec);
  }

  @Test(timeout = 600_000)
  public void incrementalGenerationWithPict() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "pict";
    String partitionerName = "incremental";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, partitionerName);
    FactorSpace factorSpace = this.factorSpace();
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithPict(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength));
    StopWatch<PeerjExperimentIncrementalParameterizedWithPict, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalPictExperiment", (PeerjExperimentIncrementalParameterizedWithPict self) ->
            extendWithPict(
                baseDir,
                factorSpace,
                base,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength)),
        (PeerjExperimentIncrementalParameterizedWithPict self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }
}
