package com.github.dakusui.jcunit8.tests.validation;

import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.tests.validation.testclassesundertest.InvalidParameterSourceMethods;
import com.github.dakusui.jcunit8.tests.validation.testclassesundertest.ParameterSourceOverloaded;
import com.github.dakusui.jcunit8.tests.validation.testclassesundertest.UndefinedConstraint;
import com.github.dakusui.jcunit8.tests.validation.testclassesundertest.UndefinedParameterReferenced;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.jcunit8.testutils.UTBase.matcher;
import static com.github.dakusui.jcunit8.testutils.UTBase.name;

public class ValidationTest {
  @Test
  public void givenUndefinedParameterReferenced$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(UndefinedParameterReferenced.class),
        matcher(
            result -> !result.wasSuccessful(),
            result -> result.getFailures().stream().allMatch(failure -> failure.getMessage().contains("'a'") &&
                failure.getMessage().contains(ParameterSource.class.getSimpleName()) &&
                failure.getMessage().contains(UndefinedParameterReferenced.class.getSimpleName()))
        )
    );
  }

  @Test
  public void givenOverloadedParameterSourceMethod$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(ParameterSourceOverloaded.class),
        matcher(
            result -> !result.wasSuccessful(),
            result -> result.getFailures().size() == 1,
            result -> result.getFailures().stream()
                .findFirst()
                .map(Failure::getMessage)
                .filter(s -> s.contains("'a'"))
                .filter(s -> s.contains("must not have any parameter"))
                .isPresent()
        )
    );
  }

  @Test
  public void givenInvalidParameterSourceMethods$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(InvalidParameterSourceMethods.class),
        matcher(
            name("not successful", result -> !result.wasSuccessful()),
            name("3 failures", result -> result.getFailureCount() == 3),
            name("1st failure", result -> result.getFailures().get(0).getMessage().contains("'a' must not have any parameter")),
            name("2nd failure", result -> result.getFailures().get(1).getMessage().contains("'b' must be public")),
            name("3rd failure", result -> result.getFailures().get(2).getMessage().contains("'c' must not be static "))
        )
    );
  }

  @Test
  public void givenInvalidReferencesToConstraints$whenRunTestClass$thenAppropriateExceptionThrown() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(UndefinedConstraint.class),
        matcher(
            name("not successful", result -> !result.wasSuccessful()),
            name("2 failures", result -> result.getFailureCount() == 2),
            name("1st failure", result -> result.getFailures().get(0).getMessage().contains("'undefinedConstraint' was not found")),
            name("2nd failure", result -> result.getFailures().get(1).getMessage().contains("'malformedConstraint!' is not a valid condition name"))
        )
    );
  }
}
