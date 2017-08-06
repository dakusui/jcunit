package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.crest.core.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.*;
import static java.util.stream.Collectors.toList;

public class JoinExperiment {
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

    @Override
    public String toString() {
      return String.format(
          "%d - %d: lhs:[size=%d,time=%d],rhs:[size=%d,time=%d],merged:[size=%d,time%d],joined:[size=%d,time=%d]",
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

      public Builder timeLhs(long time) {
        this.timeLhs = time;
        return this;
      }

      public Builder timeRhs(long time) {
        this.timeRhs = time;
        return this;
      }

      public Builder timeMerged(long time) {
        this.timeMerged = time;
        return this;
      }

      public Builder timeJoining(long time) {
        this.timeJoining = time;
        return this;
      }

      public Builder sizeLhs(int size) {
        this.sizeLhs = size;
        return this;
      }

      public Builder sizeRhs(int size) {
        this.sizeRhs = size;
        return this;
      }

      public Builder sizeMerged(int size) {
        this.sizeMerged = size;
        return this;
      }

      public Builder sizeJoining(int size) {
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

  @Test
  public void exerciseExperiment() {
    // warm up
    report(2, 2, 10);
    for (int i = 5; i < 20; i += 5) {
      for (int j = 0; j < 10; j ++) {
        System.out.println(report(2, 2, i));
      }
    }
  }

  @Test
  public void smallAndSmall() {
    List<Tuple> lhs = generateWithIpoGplus(
        factorSpace(
            parameters(
                p("a", 0, 1),
                p("b", 0, 1),
                p("c", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("lhs");
    lhs.forEach(System.out::println);

    List<Tuple> rhs = generateWithIpoGplus(
        factorSpace(
            parameters(
                p("x", 0, 1),
                p("y", 0, 1),
                p("z", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("rhs");
    rhs.forEach(System.out::println);

    System.out.println("lhs x rhs");
    List<Tuple> joined = join(lhs, rhs, 2);

    joined.forEach(System.out::println);
    System.out.println(joined.size());
  }


  @Test
  public void smallAndSmallAtOnce() {
    List<Tuple> lhs = generateWithIpoGplus(
        factorSpace(
            parameters(
                p("a", 0, 1),
                p("b", 0, 1),
                p("c", 0, 1),
                p("x", 0, 1),
                p("y", 0, 1),
                p("z", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("lhs");
    lhs.forEach(System.out::println);
  }


  @Test
  public void medAndMed() {
    List<Parameter> lhsParameters;
    List<Tuple> lhs = generateWithIpoGplus(
        factorSpace(
            lhsParameters = parameters(
                p("a", 0, 1),
                p("b", 0, 1),
                p("c", 0, 1),
                p("d", 0, 1),
                p("e", 0, 1),
                p("f", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("lhs");
    lhs.forEach(System.out::println);

    List<Parameter> rhsParameters;
    List<Tuple> rhs = generateWithIpoGplus(
        factorSpace(
            rhsParameters = parameters(
                p("x", 0, 1),
                p("y", 0, 1),
                p("z", 0, 1),
                p("w", 0, 1),
                p("v", 0, 1),
                p("u", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("rhs");
    rhs.forEach(System.out::println);

    System.out.println("lhs x rhs");
    List<Tuple> joined = join(lhs, rhs, 2);

    joined.forEach(System.out::println);
    System.out.println(joined.size());

    List<Tuple> notCovered = subtract(
        allPossibleTuples(
            2,
            Stream.concat(
                lhsParameters.stream(),
                rhsParameters.stream()
            ).collect(
                toList()
            )
        ),
        coveredTuples(2, joined)
    );

    System.out.println("notCovered");
    notCovered.forEach(System.out::println);
    System.out.println(notCovered.size());
  }


  @Test
  public void medAndMedAtOnce() {
    List<Tuple> lhs = generateWithIpoGplus(
        factorSpace(
            parameters(
                p("a", 0, 1),
                p("b", 0, 1),
                p("c", 0, 1),
                p("d", 0, 1),
                p("e", 0, 1),
                p("f", 0, 1),
                p("x", 0, 1),
                p("y", 0, 1),
                p("z", 0, 1),
                p("w", 0, 1),
                p("v", 0, 1),
                p("u", 0, 1)
            ),
            constraints(
            )
        ),
        2
    );
    System.out.println("lhs");
    lhs.forEach(System.out::println);

    System.out.println(lhs.size());
  }

  @Test
  public void test() {
    FactorSpace lhsFactorSpace;
    List<Tuple> lhs = generateWithIpoGplus(
        lhsFactorSpace = createFactorSpace("F", 2, 6),
        2
    );

    assertCoveringArray(lhs, lhsFactorSpace);
  }

  @Test
  public void test2$2$30() {
    System.out.println(report(2, 2, 50));
  }

  @Test
  public void test2$2$25() {
    System.out.println(report(2, 2, 25));
  }

  @Test
  public void test2$2$20() {
    System.out.println(report(2, 2, 20));
  }

  @Test
  public void test2$2$15() {
    System.out.println(report(2, 2, 15));
  }

  @Test
  public void test2$2$10() {
    System.out.println(report(2, 2, 10));
  }

  @Test
  public void test2$2$5() {
    System.out.println(report(2, 2, 5));
  }

  @Test
  public void test2$2$4() {
    System.out.println(report(2, 2, 4));
  }

  @Test
  public void test2$2$3() {
    System.out.println(report(2, 2, 3));
  }

  @Test
  public void test2$2$2() {
    System.out.println(report(2, 2, 2));
  }

  @Test
  public void uneven100a() {
    System.out.println(report(2, 2, 90, 10));
  }

  @Test
  public void uneven100b() {
    System.out.println(report(2, 2, 80, 20));
  }

  @Test
  public void uneven100c() {
    System.out.println(report(2, 2, 75, 25));
  }

  @Test
  public void uneven100d() {
    System.out.println(report(2, 2, 70, 40));
  }

  @Test
  public void uneven100e() {
    System.out.println(report(2, 2, 60, 40));
  }

  @Test
  public void uneven60a() {
    System.out.println(report(2, 2, 40, 20));
  }

  @Test
  public void even50() {
    System.out.println(report(2, 2, 50, 50));
  }

  @Test
  public void even25() {
    System.out.println(report(2, 2, 25, 25));
  }


  @Test
  public void even13() {
    System.out.println(report(2, 2, 13, 13));
  }


  @Test
  public void even10() {
    System.out.println(report(2, 2, 10, 10));
  }


  private Report report(int strength, int numLevels, int numFactors) {
    return report(strength, numLevels, numFactors, numFactors);
  }

  private Report report(int strength, int numLevels, int numFactorsLhs, int numFactorsRhs) {
    Report.Builder reportBuilder = new Report.Builder(numFactorsLhs, numFactorsRhs);
    StopWatch stopWatch = new StopWatch();

    FactorSpace lhsFactorSpace = createFactorSpace("F", numLevels, numFactorsLhs);
    List<Tuple> lhs = generateWithIpoGplus(
        lhsFactorSpace,
        strength
    );
    reportBuilder.timeLhs(stopWatch.get()).sizeLhs(lhs.size());

    FactorSpace rhsFactorSpace = createFactorSpace("G", numLevels, numFactorsRhs);
    List<Tuple> rhs = generateWithIpoGplus(
        rhsFactorSpace,
        strength
    );
    reportBuilder.timeRhs(stopWatch.get()).sizeRhs(rhs.size());

    FactorSpace mergedFactorSpace = mergeFactorSpaces(lhsFactorSpace, rhsFactorSpace);
    List<Tuple> merged = generateWithIpoGplus(
        mergedFactorSpace,
        strength
    );
    reportBuilder.timeMerged(stopWatch.get()).sizeMerged(merged.size());
    List<Tuple> joined = join(lhs, rhs, strength);
    reportBuilder.timeJoining(stopWatch.get()).sizeJoining(joined.size());

    assertCoveringArray(lhs, lhsFactorSpace);
    assertCoveringArray(rhs, rhsFactorSpace);
    assertCoveringArray(merged, mergedFactorSpace);
    assertCoveringArray(joined, mergedFactorSpace);

    return reportBuilder.build();
  }

  private void assertCoveringArray(List<Tuple> coveringArray, FactorSpace factorSpace) {
    //    System.out.println("== " + coveringArray.size() + " ==");
    //    coveringArray.forEach(System.out::println);

    assertThat(
        coveringArray,
        asListOf(
            Tuple.class,
            Printable.function(
                "coveredTuples",
                (List<Tuple> ca) -> coveredTuples(2, ca)
            )
        ).containsAll(
            allPossibleTuplesInFactors(
                2,
                factorSpace.getFactors())
        ).$()
    );
  }
}
