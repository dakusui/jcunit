package com.github.dakusui.jcunit8.experiments.sandboxes;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.experiments.compat.CompatJoinExperimentUtils;
import com.github.dakusui.jcunit8.experiments.compat.CompatJoinReport;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.*;
import static java.util.stream.Collectors.toList;

public class JoinSandbox1 {

  @Test
  public void exerciseExperimentsFrom5To50() {
    System.out.println(CompatJoinReport.header());
    for (int i = 50; i <= 50; i += 5) {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(i, i);
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
    List<Tuple> joined = join(lhs, rhs, Joiner.Standard::new, 2);

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
    List<Tuple> joined = join(lhs, rhs, Joiner.Standard::new, 2);

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

    assertCoveringArray(lhs, lhsFactorSpace, 2);
  }

  @Test
  public void test2$2$30() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 50));
  }

  @Test
  public void test2$2$25() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 25));
  }

  @Test
  public void test2$2$20() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 20));
  }

  @Test
  public void test2$2$15() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 15));
  }

  @Test
  public void test2$2$10() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 10));
  }

  @Test
  public void test3$2$10() {
    System.out.println(CompatJoinReport.header());
    System.out.println(CompatJoinExperimentUtils.exercise(3, 2, 5));
  }

  @Test
  public void test2$3$10() {
    System.out.println(CompatJoinReport.header());
    System.out.println(CompatJoinExperimentUtils.exercise(2, 3, 5));
  }

  @Test
  public void test2$2$5() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 5));
  }

  @Test
  public void test2$2$4() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 4));
  }

  @Test
  public void test2$2$3() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 3));
  }

  @Test
  public void test2$2$2() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 2));
  }

  @Test
  public void uneven100a() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 90, 10));
  }

  @Test
  public void uneven100b() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 80, 20));
  }

  @Test
  public void uneven100c() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 75, 25));
  }

  @Test
  public void uneven100d() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 70, 40));
  }

  @Test
  public void uneven100e() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 60, 40));
  }

  @Test
  public void uneven60a() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 40, 20));
  }

  @Test
  public void even50() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 50, 50));
  }

  @Test
  public void even25() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 25, 25));
  }


  @Test
  public void even13() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 13, 13));
  }


  @Test
  public void even10() {
    System.out.println(CompatJoinExperimentUtils.exercise(2, 2, 10, 10));
  }


}
