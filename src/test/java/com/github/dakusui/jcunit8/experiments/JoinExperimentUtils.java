package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.crest.core.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.*;

enum JoinExperimentUtils {
  ;

  static void assertCoveringArray(List<Tuple> coveringArray, FactorSpace factorSpace, int strength) {
    //    System.out.println("== " + coveringArray.size() + " ==");
    //    coveringArray.forEach(System.out::println);

    assertThat(
        coveringArray,
        asListOf(
            Tuple.class,
            Printable.function(
                "coveredTuples",
                (List<Tuple> ca) -> coveredTuples(strength, ca)
            )
        ).containsAll(
            allPossibleTuplesInFactors(
                strength,
                factorSpace.getFactors())
        ).$()
    );
  }

  static void exerciseStandardExperiment10Times(int numLhsFactors, int numRhsFactors) {
    // warm up
    exercise(2, 2, numLhsFactors);
    for (int j = 0; j < 10; j++) {
      System.out.println(exercise(2, 2, numLhsFactors, numRhsFactors));
    }
  }

  static Report exercise(int strength, int numLevels, int numFactors) {
    return exercise(strength, numLevels, numFactors, numFactors);
  }

  static Report exercise(int strength, int numLevels, int numFactorsLhs, int numFactorsRhs) {
    Report.Builder reportBuilder = new Report.Builder(numFactorsLhs, numFactorsRhs);
    StopWatch stopWatch = new StopWatch();

    FactorSpace lhsFactorSpace = createFactorSpace("F", numLevels, numFactorsLhs);
    List<Tuple> lhs = generateWithIpoGplus(
        lhsFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeLhs(stopWatch.get()).sizeLhs(lhs.size());

    FactorSpace rhsFactorSpace = createFactorSpace("G", numLevels, numFactorsRhs);
    List<Tuple> rhs = generateWithIpoGplus(
        rhsFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeRhs(stopWatch.get()).sizeRhs(rhs.size());

    FactorSpace mergedFactorSpace = mergeFactorSpaces(lhsFactorSpace, rhsFactorSpace);
    List<Tuple> merged = generateWithIpoGplus(
        mergedFactorSpace,
        strength
    );
    reportBuilder = reportBuilder.timeMerged(stopWatch.get()).sizeMerged(merged.size());
    List<Tuple> joined = join(lhs, rhs, strength);
    reportBuilder = reportBuilder.timeJoining(stopWatch.get()).sizeJoining(joined.size());

    assertCoveringArray(lhs, lhsFactorSpace, strength);
    assertCoveringArray(rhs, rhsFactorSpace, strength);
    assertCoveringArray(merged, mergedFactorSpace, strength);
    assertCoveringArray(joined, mergedFactorSpace, strength);

    return reportBuilder.build();
  }

  static class StopWatch {
    long last = System.currentTimeMillis();

    long get() {
      return -last + (last = System.currentTimeMillis());
    }

    public static void main(String... args) throws InterruptedException {
      StopWatch stopWatch = new StopWatch();
      Thread.sleep(100);
      System.out.println(stopWatch.get());
    }
  }

  static class Report {
    final         long timeLhsGeneration;
    final         long timeRhsGeneration;
    final         long timeMergedGeneration;
    final         long timeJoining;
    final         int  sizeLhs;
    final         int  sizeRhs;
    final         int  sizeMerged;
    final         int  sizeJoined;
    private final int  lhsNumFactors;
    private final int  rhsNumFactors;

    Report(int lhsNumFactors, int rhsNumFactors, long timeLhsGeneration, long timeRhsGeneration, long timeMergedGeneration, long timeJoining, int sizeLhs, int sizeRhs, int sizeMerged, int sizeJoined) {
      this.lhsNumFactors = lhsNumFactors;
      this.rhsNumFactors = rhsNumFactors;
      this.timeLhsGeneration = timeLhsGeneration;
      this.timeRhsGeneration = timeRhsGeneration;
      this.timeMergedGeneration = timeMergedGeneration;
      this.timeJoining = timeJoining;
      this.sizeLhs = sizeLhs;
      this.sizeRhs = sizeRhs;
      this.sizeMerged = sizeMerged;
      this.sizeJoined = sizeJoined;
    }

    static String header() {
      return "lhsNumFactors,rhsNumFactors,sizeLhs,timeLhsGeneration,sizeRhs,timeRhsGeneration,sizeMerged,timeMergedGeneration,sizeJoined,timeJoining";
    }

    @Override
    public String toString() {
      /*
      return String.format(
          "%d - %d: lhs:[size=%d,time=%d],rhs:[size=%d,time=%d],merged:[size=%d,time%d],joined:[size=%d,time=%d]",
          this.lhsNumFactors, this.rhsNumFactors,
          this.sizeLhs, this.timeLhsGeneration,
          this.sizeRhs, this.timeRhsGeneration,
          this.sizeMerged, this.timeMergedGeneration,
          this.sizeJoined, this.timeJoining
      );
      */
      return String.format(
          "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
          this.lhsNumFactors, this.rhsNumFactors,
          this.sizeLhs, this.timeLhsGeneration,
          this.sizeRhs, this.timeRhsGeneration,
          this.sizeMerged, this.timeMergedGeneration,
          this.sizeJoined, this.timeJoining
      );
    }

    static class Builder {
      private final int  lhsNumFactors;
      private final int  rhsNumFactors;
      private       long timeLhs;
      private       long timeRhs;
      private       long timeMerged;
      private       long timeJoining;
      private       int  sizeLhs;
      private       int  sizeRhs;
      private       int  sizeMerged;
      private       int  sizeJoining;

      public Builder(int lhsNumFactors, int rhsNumFactors) {
        this.lhsNumFactors = lhsNumFactors;
        this.rhsNumFactors = rhsNumFactors;
      }

      Builder timeLhs(long time) {
        this.timeLhs = time;
        return this;
      }

      Builder timeRhs(long time) {
        this.timeRhs = time;
        return this;
      }

      Builder timeMerged(long time) {
        this.timeMerged = time;
        return this;
      }

      Builder timeJoining(long time) {
        this.timeJoining = time;
        return this;
      }

      Builder sizeLhs(int size) {
        this.sizeLhs = size;
        return this;
      }

      Builder sizeRhs(int size) {
        this.sizeRhs = size;
        return this;
      }

      Builder sizeMerged(int size) {
        this.sizeMerged = size;
        return this;
      }

      Builder sizeJoining(int size) {
        this.sizeJoining = size;
        return this;
      }

      Report build() {
        return new Report(
            this.lhsNumFactors,
            this.rhsNumFactors,
            this.timeLhs,
            this.timeRhs,
            this.timeMerged,
            this.timeJoining,
            this.sizeLhs,
            this.sizeRhs,
            this.sizeMerged,
            this.sizeJoining
        );
      }
    }

  }
}
