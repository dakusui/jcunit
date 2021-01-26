package com.github.dakusui.peerj.ut.runners;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.peerj.ext.shared.IoUtils;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.testbases.PeerJExperimentParameterized;
import com.github.dakusui.peerj.testbases.StopWatch;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class PeerJExperimentIncrementalParameterized extends PeerJExperimentParameterized {
  public PeerJExperimentIncrementalParameterized(Spec spec) {
    super(spec);
  }

  public static List<Spec> parametersWith(int strength, ConstraintHandlingMethod constraintHandlingMethod, int begin, int end) {
    return parametersWith(strength, -1, constraintHandlingMethod, begin, end);
  }

  public static List<Spec> parametersWith(int baseStrength, int relationStrength, ConstraintHandlingMethod constraintHandlingMethod, int begin, int end) {
    int startInclusive = begin / 20;
    int endExclusive = end / 20;
    return IntStream.range(startInclusive, endExclusive)
        .map(i -> i * 20)
        .boxed()
        .flatMap(i -> Arrays.stream(ConstraintSet.values())
            .map(constraintSet -> new Spec.Builder()
                .strength(baseStrength)
                .degree(i)
                .rank(4)
                .constraintSet(constraintSet)
                .constraintHandlingMethod(constraintHandlingMethod)
                .relationStrength(relationStrength)
                .build()))
        .collect(toList());
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
    StopWatch<PeerJExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalActsExperiment", (PeerJExperimentParameterized self) ->
            extendWithActs(
                baseDir,
                factorSpace,
                base,
                factorSpace.relationStrength() >= 0
                    ? -1
                    : strength,
                algorithm(),
                constraintHandlingMethod())),
        (PeerJExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, partitionerName), Stream.of(stopWatch.report()));
    }
  }

  @Test
  public void incrementalGenerationWithWeakenProductCombinatorialJoin() {
    String dataSetName = this.dataSetName();
    int strength = strength();
    String generationMode = "join";
    String suffix = "incremental";
    File baseDir = baseDirFor(dataSetName, this.strength(), generationMode, suffix);
    FactorSpace factorSpace = this.factorSpace();
    Requirement requirement = requirement(strength);
    SchemafulTupleSet base = SchemafulTupleSet.fromTuples(generateWithActs(new File(baseDir, "base"), baseFactorSpaceFrom(factorSpace), strength, algorithm(), constraintHandlingMethod()));
    StopWatch<PeerJExperimentParameterized, List<Tuple>> stopWatch = new StopWatch<>(
        Printable.function("conductIncrementalJoinExperiment", (PeerJExperimentParameterized self) -> extendWithCombinatorialJoin(requirement, baseDir, factorSpace, base, algorithm(), constraintHandlingMethod())),
        (PeerJExperimentParameterized self) -> format("[%s]", self.spec),
        (List<Tuple> result) -> format("[size:%s]", result.size()));
    try {
      stopWatch.apply(this);
    } finally {
      IoUtils.writeTo(resultFile(dataSetName, strength(), generationMode, suffix), Stream.of(stopWatch.report()));
    }
  }

  private static FactorSpace baseFactorSpaceFrom(FactorSpace factorSpace) {
    int baseDegree = factorSpace.getFactors().size() - 10;
    List<Factor> baseFactors = factorSpace.getFactors().subList(0, baseDegree);
    Set<String> baseFactorNames = baseFactors.stream().map(Factor::getName).collect(toSet());
    return FactorSpace.create(
        baseFactors,
        factorSpace.getConstraints()
            .stream()
            .filter(each -> each.involvedKeys().stream().anyMatch(baseFactorNames::contains))
            .peek(crossingConstraintFound(baseFactorNames))
            .collect(toList()));
  }
}
