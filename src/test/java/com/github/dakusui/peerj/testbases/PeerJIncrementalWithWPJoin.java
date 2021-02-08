package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;

public class PeerJIncrementalWithWPJoin extends PeerJIncremental {
  public PeerJIncrementalWithWPJoin(Spec spec) {
    super(spec);
  }

  @Test(timeout = 3000_000)
  public void incrementalGenerationWithWeakenProductCombinatorialJoin() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join";
    String suffix = "incremental";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, suffix);
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithActs(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength, algorithm(), constraintHandlingMethod()));
    StopWatch<PeerJBase, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalJoinExperiment", (PeerJBase self) -> extendWithCombinatorialJoin(requirement, baseDir, factorSpace, base, algorithm(), constraintHandlingMethod())),
        (PeerJBase self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, suffix), Stream.of(stopWatch.report()));
    }
  }
}
