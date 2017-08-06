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
        lhsFactorSpace = createFactorSpace("F", 6, 2),
        2
    );

    assertCoveringArray(lhs, lhsFactorSpace);
  }


  @Test
  public void testJoin() {
    int strength = 2;
    System.out.println("lhsGeneration started:" + System.currentTimeMillis());
    FactorSpace lhsFactorSpace = createFactorSpace("F", 50, 2);
    List<Tuple> lhs = generateWithIpoGplus(
        lhsFactorSpace,
        strength
    );
    System.out.println("lhsGeneration finished:" + System.currentTimeMillis());

    System.out.println("lhsGeneration started:" + System.currentTimeMillis());
    FactorSpace rhsFactorSpace = createFactorSpace("G", 50, 2);
    List<Tuple> rhs = generateWithIpoGplus(
        rhsFactorSpace,
        strength
    );
    System.out.println("rhsGeneration finished:" + System.currentTimeMillis());

    FactorSpace mergedFactorSpace = mergeFactorSpaces(lhsFactorSpace, rhsFactorSpace);
    //    List<Tuple> merged = generateWithIpoGplus(
    //        mergedFactorSpace,
    //        strength
    //    );
    System.out.println("joinOperation started:" + System.currentTimeMillis());
    List<Tuple> joined = join(lhs, rhs, strength);
    System.out.println("joinOperation finished:" + System.currentTimeMillis());

    assertCoveringArray(lhs, lhsFactorSpace);
    assertCoveringArray(rhs, rhsFactorSpace);
    //    assertCoveringArray(merged, mergedFactorSpace);
    assertCoveringArray(joined, mergedFactorSpace);
  }

  private void assertCoveringArray(List<Tuple> coveringArray, FactorSpace factorSpace) {
    System.out.println("== " + coveringArray.size() + " ==");
    coveringArray.forEach(System.out::println);

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
