package com.github.dakusui.jcunit8.tests.features.pipeline.negative;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import com.github.dakusui.jcunit8.testutils.PipelineTestBase;
import com.github.dakusui.jcunit8.testutils.TestSuiteUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Test;

import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class NegativeGenerationTest extends PipelineTestBase {
  @Test
  public void test() {
    Constraint constraint = Constraint.create(
        "simple1!=simple2",
        tuple -> !Objects.equals(tuple.get("simple1"), tuple.get("simple2")),
        "simple1",
        "simple2"
    );
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            asList(
                simpleParameterFactory("Hello", "world", "everyone").create("simple1"),
                simpleParameterFactory("Hello", "world", "everyone").create("simple2")
            ),
            singletonList(constraint)
        ),
        UTUtils.matcher(
            UTUtils.oracle("More than 1 test cases",
                (TestSuite testCases) -> testCases.size() > 1),
            UTUtils.oracle("Only last one is negative.",
                (TestSuite testCases) -> {
                  for (int i = 0; i < testCases.size(); i++) {
                    if (i == testCases.size() - 1) {
                      if (testCases.get(i).getCategory() != TestCase.Category.NEGATIVE) {
                        return false;
                      }
                    } else {
                      if (testCases.get(i).getCategory() != TestCase.Category.REGULAR) {
                        return false;
                      }
                    }
                  }
                  return true;
                }),
            UTUtils.oracle("Last one holds given constraint as an element of violated ones",
                (TestSuite testSuite) ->
                    Objects.equals(
                        testSuite.get(testSuite.size() - 1).violatedConstraints(),
                        singletonList(constraint)
                    )),
            UTUtils.oracle("Constraint violations happen as specified by category",
                (TestSuite testCases) ->
                    testCases.stream()
                        .allMatch(tupleTestCase -> tupleTestCase.getCategory() == TestCase.Category.REGULAR ?
                            constraint.test(tupleTestCase.getTestInput()) :
                            tupleTestCase.getCategory() == TestCase.Category.NEGATIVE &&
                                tupleTestCase.violatedConstraints().stream()
                                    .noneMatch(constraint1 -> constraint1.test(tupleTestCase.getTestInput()))
                        ))
        ));
  }

  @Override
  public Requirement requirement() {
    return new Requirement.Builder()
        .withNegativeTestGeneration(true)
        .build();
  }
}
