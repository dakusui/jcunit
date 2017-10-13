package com.github.dakusui.jcunit8.tests.validation;

import com.github.dakusui.crest.Crest;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.tests.validation.testresources.*;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.MissingParameter;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.TypeMismatch;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.UnknownParameter;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.crest.core.Printable.function;
import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;
import static org.junit.Assert.assertThat;

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
                ".getFailures().getTestInput(0).getMessage()", result -> result.getFailures().get(0).getMessage(), ".contains('undefinedConstraint' was not found)",
                v -> v.contains("'undefinedConstraint' was not found")),
            oracle(
                ".getFailures().getTestInput(1).getMessage()",
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
                "==1",
                v -> v == 1
            ),
            oracle(
                "{x}.getFailures().getTestInput(0).getException()",
                result -> result.getFailures().get(0).getException(),
                "",
                throwable -> throwable instanceof TestDefinitionException
            ),
            oracle(
                "{x}.getFailures().getTestInput(0).getMessage()",
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
            oracle("{x}.getFailures().getTestInput(0).getMessage()", result -> result.getFailures().get(0).getMessage(),
                "containing 'No parameter'", message -> message.contains("No parameter is found"))
        )
    );
  }


  @Test
  public void typeCompatibilityTest1() {
    Crest.assertThat(
        JUnitCore.runClasses(IncompatibleParameters.IncompatibleType.class),
        allOf(
            asBoolean(
                function("wasSuccessful", Result::wasSuccessful)
            ).isFalse(
            ).$(),
            asInteger(
                function("getRunCount", Result::getRunCount)
            ).equalTo(
                1
            ).$(),
            asString(
                function("getFailures().getTestInput(0).getMessage()", (Result result) -> result.getFailures().get(0).getMessage())
            ).equalTo(
                "'100' is not compatible with parameter 0 of 'testMethod(String)'"
            ).$()
        )
    );
  }

  @Test
  public void typeCompatibilityTest2() {
    Crest.assertThat(
        JUnitCore.runClasses(IncompatibleParameters.CompatibleNullValue.class),
        allOf(
            asBoolean(function(
                "wasSuccessful", Result::wasSuccessful
            )).isTrue().$(),
            asInteger(function(
                "getRunCount", Result::getRunCount
            )).equalTo(1).$()
        )
    );
  }

  @Test
  public void typeCompatibilityTest3() {
    Crest.assertThat(
        JUnitCore.runClasses(IncompatibleParameters.IncompatiblePrimitiveType.class),
        allOf(
            asBoolean(
                function("wasSuccessful", Result::wasSuccessful)
            ).isFalse(
            ).$(),
            asInteger(
                function("getRunCount", Result::getRunCount)
            ).equalTo(
                1
            ).$(),
            asString(
                function("getFailures().getTestInput(0).getMessage()", (Result result) -> result.getFailures().get(0).getMessage())
            ).equalTo(
                "'1' is not compatible with parameter 0 of 'testMethod(boolean)'"
            ).$()
        )
    );
  }

  @Test
  public void typeCompatibilityTest4() {
    Crest.assertThat(
        JUnitCore.runClasses(IncompatibleParameters.IncompatibleNullValue.class),
        allOf(
            asBoolean(
                function("wasSuccessful", Result::wasSuccessful)
            ).isFalse(
            ).$(),
            asInteger(
                function("getRunCount", Result::getRunCount)
            ).equalTo(
                1
            ).$(),
            asString(
                function("getFailures().getTestInput(0).getMessage()", (Result result) -> result.getFailures().get(0).getMessage())
            ).equalTo(
                "'null' is not compatible with parameter 0 of 'testMethod(int)'"
            ).$()
        )
    );
  }

  @Test
  public void noTest() {
    Result result = JUnitCore.runClasses(NoTestMethod.class);
    assertThat(
        result.getFailures().get(0).getMessage(),
        CoreMatchers.containsString("No runnable methods")
    );
  }

  @Test
  public void missingParameterInSeed() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(
            MissingParameter.class
        ),
        matcher(
            oracle("{x}.wasSuccessful", Result::wasSuccessful, "==false", v -> !v),
            oracle("{x}.getRunCount()", Result::getRunCount, "==1", v -> v == 1),
            oracle(
                "{x}.getFailures().getTestInput(0).getMessage()",
                result -> result.getFailures().get(0).getMessage(),
                "contains'Parameter(s) were not found: [parameter2] in tuple: {parameter1=hello}'",
                v -> v.contains("Parameter(s) were not found: [parameter2] in tuple: {parameter1=hello}")
            )
        )
    );
  }


  @Test
  public void unknownParameterInSeed() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(
            UnknownParameter.class
        ),
        matcher(
            oracle("{x}.wasSuccessful", Result::wasSuccessful, "==false", v -> !v),
            oracle("{x}.getRunCount()", Result::getRunCount, "==1", v -> v == 1),
            oracle(
                "{x}.getFailures().getTestInput(0).getMessage()",
                result -> result.getFailures().get(0).getMessage(),
                "contains'[unknownParameter] in tuple: {parameter1=hello, parameter2=hello, unknownParameter=hello}'",
                v -> v.contains("[unknownParameter] in tuple: {parameter1=hello, parameter2=hello, unknownParameter=hello}")
            )
        )
    );

  }

  @Test
  public void typeMismatchInSeed() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(
            TypeMismatch.class
        ),
        matcher(
            oracle("{x}.wasSuccessful", Result::wasSuccessful, "==false", v -> !v),
            oracle("{x}.getRunCount()", Result::getRunCount, "==5", v -> v == 5),
            oracle(
                "{x}.getFailures().getTestInput(0).getMessage()",
                result -> result.getFailures().get(0).getMessage(),
                "contains'is not compatible with parameter 1 of 'test(String,String)''",
                v -> v.contains("is not compatible with parameter 1 of 'test(String,String)'")
            )
        )
    );
  }
}
