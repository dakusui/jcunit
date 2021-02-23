package com.github.dakusui.jcunit8.tests.components.ext;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.utils.PeerJUtils2;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.testbases.StopWatch;
import com.github.dakusui.peerj.testbases.PeerJIncremental;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.peerj.testbases.ExperimentBase.extendWithPict;
import static com.github.dakusui.peerj.testbases.ExperimentBase.generateWithPict;
import static java.lang.String.format;

public class PictTest {
  @Test(timeout = 600_000)
  public void incrementalGenerationWithPict() {
    int strength = 2;
    String dataSetName = "unknown";//this.dataSetName();
    String generationMode = "pict";
    String partitionerName = "incremental";
    FactorSpace factorSpace = new FactorSpaceSpec("F").addFactors(2, 30).toFactorSpace();
    File baseDir = PeerJUtils2.baseDirFor(dataSetName, strength, generationMode, partitionerName);
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithPict(new File(baseDir, "base"), PeerJIncremental.baseFactorSpaceFrom(factorSpace), strength));
    StopWatch<PictTest, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalActsExperiment", (PictTest self) ->
            extendWithPict(
                baseDir,
                factorSpace,
                base,
                strength)),
        (PictTest self) -> format("[t=%s]", strength),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      assertThat(stopWatch.apply(this), asListOf(Tuple.class).isNotEmpty().$());
    } finally {
      IoUtils.writeTo(PeerJUtils2.resultFile(dataSetName, strength, generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }
}
