package com.github.dakusui.jcunit8.tests.components.ext;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.PeerJUtils2;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.testbases.ExperimentBase;
import com.github.dakusui.peerj.testbases.StopWatch;
import com.github.dakusui.peerj.testbases.PeerJIncremental;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.peerj.testbases.ExperimentBase.Algorithm.IPOG;
import static com.github.dakusui.peerj.testbases.ExperimentBase.ConstraintHandlingMethod.SOLVER;
import static com.github.dakusui.peerj.testbases.ExperimentBase.extendWithActs;
import static java.lang.String.format;

public class ActsTest {
  @Test(timeout = 600_000)
  public void incrementalGenerationWithActs() {
    int strength = 2;
    String dataSetName = "unknown";//this.dataSetName();
    String generationMode = "acts";
    String partitionerName = "incremental";
    FactorSpace factorSpace = new FactorSpaceSpec("F").addFactors(2, 30).toFactorSpace();
    ExperimentBase.Algorithm algorithm = IPOG;
    ExperimentBase.ConstraintHandlingMethod constraintHandlingMethod = SOLVER;
    File baseDir = PeerJUtils2.baseDirFor(dataSetName, strength, generationMode, partitionerName);
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(ExperimentBase.generateWithActs(new File(baseDir, "base"), PeerJIncremental.baseFactorSpaceFrom(factorSpace), strength, algorithm, constraintHandlingMethod));
    StopWatch<ActsTest, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalActsExperiment", (ActsTest self) ->
            extendWithActs(
                baseDir,
                factorSpace,
                base,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm,
                constraintHandlingMethod)),
        (ActsTest self) -> format("[t=%s:algorithm=%s:constraintHandling=%s]", strength, algorithm, constraintHandlingMethod),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      assertThat(stopWatch.apply(this), asListOf(Tuple.class).isNotEmpty().$());
    } finally {
      IoUtils.writeTo(PeerJUtils2.resultFile(dataSetName, strength, generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }
}
