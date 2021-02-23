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
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.utils.PeerJUtils2.*;
import static java.lang.String.format;

public class PeerJIncrementalWithWPJoinBasedOnActs extends PeerJIncremental {
  public PeerJIncrementalWithWPJoinBasedOnActs(Spec spec) {
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
        conductExperiment(baseDir, factorSpace, requirement, base),
        formatSpec(),
        formatResult());
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, suffix), Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  private Function<List<Tuple>, String> formatResult() {
    return (List<Tuple> result) -> format("[size:%s]", result.size());
  }

  private Function<PeerJBase, String> formatSpec() {
    return (PeerJBase self) -> format("[%s]", self.spec);
  }

  private Function<PeerJBase, List<Tuple>> conductExperiment(File baseDir, FactorSpace factorSpace, Requirement requirement, SchemafulTupleSet base) {
    return Printable.function("conductIncrementalJoinExperiment", (PeerJBase self) -> extendWithCombinatorialJoin(requirement, baseDir, factorSpace, base, algorithm(), constraintHandlingMethod()));
  }
}
