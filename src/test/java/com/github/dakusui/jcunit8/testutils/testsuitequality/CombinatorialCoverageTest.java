package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.crest.core.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.crest.functions.CrestPredicates.isEmpty;
import static com.github.dakusui.jcunit8.testutils.testsuitequality.CombinatorialCoverageTestUtils.failsIf;
import static java.util.stream.Collectors.toList;

public class CombinatorialCoverageTest {
  @Test
  public void given3ParametersWith1ConstraintStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        CombinatorialCoverageTestUtils.parameters(
            CombinatorialCoverageTestUtils.p("a", 0, 1),
            CombinatorialCoverageTestUtils.p("b", 0, 1),
            CombinatorialCoverageTestUtils.p("c", 0, 1)
        ),
        CombinatorialCoverageTestUtils.constraints(
            CombinatorialCoverageTestUtils.c(tuple -> tuple.get("a").equals(tuple.get("b")), "a", "b")
        ),
        2
    );
  }

  @Test
  public void given4ParametersWith1ConstraintStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        CombinatorialCoverageTestUtils.parameters(
            CombinatorialCoverageTestUtils.p("a", 0, 1),
            CombinatorialCoverageTestUtils.p("b", 0, 1),
            CombinatorialCoverageTestUtils.p("c", 0, 1),
            CombinatorialCoverageTestUtils.p("d", 0, 1)
        ),
        CombinatorialCoverageTestUtils.constraints(
            CombinatorialCoverageTestUtils.c(tuple -> tuple.get("a").equals(tuple.get("b")), "a", "b")
        ),
        2
    );
  }


  @Test
  public void given4ParametersWithNoConstraintStrength3$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        CombinatorialCoverageTestUtils.parameters(
            CombinatorialCoverageTestUtils.p("a", 0, 1),
            CombinatorialCoverageTestUtils.p("b", 0, 1),
            CombinatorialCoverageTestUtils.p("c", 0, 1),
            CombinatorialCoverageTestUtils.p("d", 0, 1)
        ),
        CombinatorialCoverageTestUtils.constraints(
        ),
        3
    );
  }

  @Test
  public void given3ParametersWith2ConstraintThatMake1LevelInvalidStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        CombinatorialCoverageTestUtils.parameters(
            CombinatorialCoverageTestUtils.p("a", 0, 1),
            CombinatorialCoverageTestUtils.p("b", 0, 1),
            CombinatorialCoverageTestUtils.p("c", 0, 1)
        ),
        CombinatorialCoverageTestUtils.constraints(
            CombinatorialCoverageTestUtils.c(tuple -> !(tuple.get("a").equals(0) && tuple.get("b").equals(0)), "a", "b"),
            CombinatorialCoverageTestUtils.c(tuple -> !(tuple.get("a").equals(0) && tuple.get("b").equals(1)), "a", "b")
        ),
        2
    );
  }

  private void givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
      List<Parameter> parameters, List<Constraint> constraints, int strength
  ) {
    System.out.println(
        CombinatorialCoverageTestUtils.allPossibleTuples(strength, parameters)
    );
    assertTestSuite(
        CombinatorialCoverageTestUtils.buildTestSuite(
            strength,
            parameters,
            constraints
        ),
        parameters,
        strength,
        false
    );
  }

  private static void assertTestSuite(
      TestSuite testSuite, List<Parameter> parameters, int strength, @SuppressWarnings("SameParameterValue") boolean debug
  ) {
    assertThat(
        testSuite,
        allOf(
            asListOf(Tuple.class,
                Printable.function(
                    "coveredTuples",
                    (TestSuite suite) -> CombinatorialCoverageTestUtils.coveredTuples(
                        strength,
                        suite.stream().map(
                            TestCase::get
                        ).collect(
                            toList()
                        )
                    ))
            ).containsAll(
                CombinatorialCoverageTestUtils.allPossibleTuples(strength, parameters)
            ).check(
                Printable.function(
                    "tuplesNotCoveredByTestSuite",
                    (List<Tuple> coveredTuples) -> CombinatorialCoverageTestUtils.subtract(CombinatorialCoverageTestUtils.allPossibleTuples(strength, parameters), coveredTuples)
                ),
                Printable.predicate(
                    "areAllViolation",
                    (List<Tuple> missingTuples) -> missingTuples.stream().noneMatch(
                        tuple -> CombinatorialCoverageTestUtils.findAllowedSuperTupleFor(tuple, testSuite.getParameterSpace()).isPresent()
                    )
                )
            ).any(),
            asListOf(Tuple.class,
                Printable.function(
                    "toTuple",
                    (TestSuite suite) -> suite.stream().map(
                        TestCase::get
                    ).collect(
                        toList()
                    )
                )
            ).check(
                isEmpty().negate()
            ).allMatch(
                Printable.predicate(
                    "areAllValid",
                    tuple -> testSuite.getParameterSpace().getConstraints().stream().allMatch(constraint -> constraint.test(tuple))
                )
            ).$(),
            failsIf(debug).$()
        )
    );
  }

}
