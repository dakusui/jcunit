package com.github.dakusui.jcunitx.testutils.testsuitequality;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.testsuite.TestCase;
import com.github.dakusui.jcunitx.testsuite.TestSuite;
import com.github.dakusui.jcunitx.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.utils.printable.Predicates.isEmpty;
import static com.github.dakusui.jcunitx.testutils.testsuitequality.CoveringArrayGenerationUtils.c;
import static com.github.dakusui.jcunitx.testutils.testsuitequality.CoveringArrayGenerationUtils.constraints;
import static com.github.dakusui.jcunitx.testutils.testsuitequality.CoveringArrayGenerationUtils.failsIf;
import static com.github.dakusui.jcunitx.testutils.testsuitequality.CoveringArrayGenerationUtils.p;
import static com.github.dakusui.jcunitx.testutils.testsuitequality.CoveringArrayGenerationUtils.parameters;
import static java.util.stream.Collectors.toList;

public class CombinatorialCoverageTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void given3ParametersWith1ConstraintStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        parameters(
            p("a", 0, 1),
            p("b", 0, 1),
            p("c", 0, 1)
        ),
        constraints(
            c(tuple -> tuple.get("a").equals(tuple.get("b")), "a", "b")
        ),
        2
    );
  }

  @Test
  public void given4ParametersWith1ConstraintStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        parameters(
            p("a", 0, 1),
            p("b", 0, 1),
            p("c", 0, 1),
            p("d", 0, 1)
        ),
        constraints(
            c(tuple -> tuple.get("a").equals(tuple.get("b")), "a", "b")
        ),
        2
    );
  }


  @Test
  public void given4ParametersWithNoConstraintStrength3$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        parameters(
            p("a", 0, 1),
            p("b", 0, 1),
            p("c", 0, 1),
            p("d", 0, 1)
        ),
        constraints(
        ),
        3
    );
  }

  @Test
  public void given3ParametersWith2ConstraintThatMake1LevelInvalidStrength2$whenBuildTestSuite$thenValidTestSuiteBuilt() {
    givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
        parameters(
            p("a", 0, 1),
            p("b", 0, 1),
            p("c", 0, 1)
        ),
        constraints(
            c(tuple -> !(tuple.get("a").equals(0) && tuple.get("b").equals(0)), "a", "b"),
            c(tuple -> !(tuple.get("a").equals(0) && tuple.get("b").equals(1)), "a", "b")
        ),
        2
    );
  }

  private void givenParameterSpaceAndStrength$whenBuildTestSuite$thenCombinatorialCoverageFineAndNoConstraintIsViolated(
      List<Parameter<?>> parameters, List<Constraint> constraints, int strength
  ) {
    System.out.println(
        CoveringArrayGenerationUtils.allPossibleTuples(strength, parameters)
    );
    assertTestSuite(
        CoveringArrayGenerationUtils.buildTestSuite(
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
      TestSuite testSuite, List<Parameter<?>> parameters, int strength, @SuppressWarnings("SameParameterValue") boolean debug
  ) {
    assertThat(
        testSuite,
        allOf(
            asListOf(AArray.class,
                Printable.function(
                    "coveredTuples",
                    (TestSuite suite) -> CoveringArrayGenerationUtils.coveredTuples(
                        strength,
                        suite.stream().map(
                            TestCase::getTestInput
                        ).collect(
                            toList()
                        )
                    ))
            ).containsAll(
                CoveringArrayGenerationUtils.allPossibleTuples(strength, parameters)
            ).check(
                Printable.function(
                    "tuplesNotCoveredByTestSuite",
                    (List<AArray> coveredTuples) -> CoveringArrayGenerationUtils.subtract(CoveringArrayGenerationUtils.allPossibleTuples(strength, parameters), coveredTuples)
                ),
                Printable.predicate(
                    "areAllViolation",
                    (List<AArray> missingTuples) -> missingTuples.stream().noneMatch(
                        tuple -> CoveringArrayGenerationUtils.findAllowedSuperTupleFor(tuple, testSuite.getParameterSpace()).isPresent()
                    )
                )
            ).any(),
            asListOf(AArray.class,
                Printable.function(
                    "toTuple",
                    (TestSuite suite) -> suite.stream().map(
                        TestCase::getTestInput
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
