package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import org.junit.Test;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class PeerJScratchWithWPJoinBasedOnActs extends PeerJScratch {
  public PeerJScratchWithWPJoinBasedOnActs(Spec spec) {
    super(spec);
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
    StopWatch<PeerJScratchWithWPJoinBasedOnActs, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductScratchJoinExperiment(ACTS)", (PeerJScratchWithWPJoinBasedOnActs self) -> generateWithCombinatorialJoinBasedOnActs(requirement, baseDir, partitioner, factorSpace, algorithm(), constraintHandlingMethod(), "")),
        (PeerJScratchWithWPJoinBasedOnActs self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      List<Tuple> rows = stopWatch.apply(this);
      IoUtils.writeTo(
          resultCsvFile(dataSetName, strength(), generationMode, partitioner.name()),
          rows.stream()
              .map((Tuple each) -> each.keySet()
                  .stream()
                  .sorted(Comparator.comparingInt(PeerJScratchWithWPJoinBasedOnActs::keyIndexOf))
                  .map(each::get)
                  .map(Objects::toString)
                  .collect(joining(",")))
      );
    } finally {
      IoUtils.writeTo(
          resultFile(dataSetName, strength(), generationMode, partitioner.name()),
          Stream.of(stopWatch.report()).peek(System.out::println));
    }
  }

  private static int keyIndexOf(String key) {
    String[] arr1 = key.split("_");
    return Integer.parseInt(arr1[0].substring(1)) * 100 + Integer.parseInt(arr1[1].substring(1));
  }
}
