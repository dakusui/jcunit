package com.github.dakusui.jcunit8.tests.validation;

import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.tests.validation.testclassesundertest.*;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;

public class ValidationTest {
  @Test
  public void givenUndefinedParameterReferenced$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(UndefinedParameterReferenced.class),
        UTUtils.matcherFromPredicates(
            oracle("was not successful", result -> !result.wasSuccessful()),
            oracle(
                "message explains the failure",
                result -> result.getFailures().stream().allMatch(failure -> failure.getMessage().contains("'a'") &&
                    failure.getMessage().contains(ParameterSource.class.getSimpleName()) &&
                    failure.getMessage().contains(UndefinedParameterReferenced.class.getSimpleName())))
        )
    );
  }

  @Test
  public void givenOverloadedParameterSourceMethod$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(ParameterSourceOverloaded.class),
        UTUtils.matcherFromPredicates(
            oracle("was not successful", result -> !result.wasSuccessful()),
            oracle("size of failures == 1", result -> result.getFailures().size() == 1),
            oracle("error contains intended message", result -> result.getFailures().stream()
                .findFirst()
                .map(Failure::getMessage)
                .filter(s -> s.contains("'a'"))
                .filter(s -> s.contains("must not have any parameter"))
                .isPresent()
            )
        )
    );
  }

  @Test
  public void givenInvalidParameterSourceMethods$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(InvalidParameterSourceMethods.class),
        UTUtils.matcherFromPredicates(
            oracle("not successful", result -> !result.wasSuccessful()),
            oracle("3 failures", result -> result.getFailureCount() == 3),
            oracle("1st failure", result -> result.getFailures().get(0).getMessage().contains("'a' must not have any parameter")),
            oracle("2nd failure", result -> result.getFailures().get(1).getMessage().contains("'b' must be public")),
            oracle("3rd failure", result -> result.getFailures().get(2).getMessage().contains("'c' must not be static "))
        )
    );
  }

  @Test
  public void givenInvalidReferencesToConstraints$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(UndefinedConstraint.class),
        matcher(
            oracle(
                "Result::wasSuccessful",
                Result::wasSuccessful,
                "==false", v -> !v
            ),
            oracle(
                "Result::getFailureCount",
                Result::getFailureCount,
                "==2",
                v -> v == 2
            ),
            oracle(
                ".getFailures().get(0).getMessage()", result -> result.getFailures().get(0).getMessage(), ".contains('undefinedConstraint' was not found)",
                v -> v.contains("'undefinedConstraint' was not found")),
            oracle(
                ".getFailures().get(1).getMessage()",
                result -> result.getFailures().get(1).getMessage(),
                "contains \"'malformedConstraint!' is not a valid condition oracle\"",
                v -> v.contains("'malformedConstraint!' is not a valid condition name"))
        )
    );
  }

  @Test
  public void givenInvalidConditions$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(InvalidConditionMethods.class),
        UTUtils.matcherFromPredicates(
            oracle("not successful", result -> !result.wasSuccessful()),
            oracle("3 runs", result -> result.getRunCount() == 3),
            oracle("3 failures", result -> result.getFailureCount() == 3),
            oracle("1st failure", result -> result.getFailures().get(0).getMessage().contains("'nonPublic' must be public")),
            oracle("2nd failure", result -> result.getFailures().get(1).getMessage().contains("'staticMethod' must not be static")),
            oracle("3rd failure", result -> result.getFailures().get(2).getMessage().contains("'wrongType' must return"))
        )
    );
  }

  @Test
  public void givenNonAnnotatedParameterInTestMethod$whenRunTest$thenAppropriateExceptionIsThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(ParameterWithoutFromAnnotation.class),
        matcher(
            oracle(
                "{x} is not successful", result -> !result.wasSuccessful()),
            oracle(
                "{x}.getFailures().size()",
                result -> result.getFailures().size(),
                "==2",
                v -> v == 2
            ),
            oracle(
                "{x}.getFailures().get(0).getException()",
                result -> result.getFailures().get(0).getException(),
                "",
                throwable -> throwable instanceof TestDefinitionException
            ),
            oracle(
                "{x}.getFailures().get(0).getMessage()",
                result -> result.getFailures().get(0).getMessage(),
                "containing '@From' and 'testMethod'",
                v -> v.contains("@From") && v.contains("testMethod")
            )
        )
    );
  }

  @Test
  public void givenNoParameterTestClass$whenRunTest$thenTestDefinitionExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(NoParameter.class),
        matcher(
            oracle("!{x}.wasSuccessful()", result -> !result.wasSuccessful()),
            oracle("{x}.getFailures().get(0).getMessage()", result -> result.getFailures().get(0).getMessage(),
                "containing 'No parameter'", message -> message.contains("No parameter is found"))
        )
    );
  }
}
