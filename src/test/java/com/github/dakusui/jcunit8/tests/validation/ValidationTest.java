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

import java.util.function.Function;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asBoolean;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.core.Printable.function;
import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;
import static org.junit.Assert.assertThat;

public class ValidationTest {
  private static Function<Result, String> exceptionMessage(int i) {
    return function(
        String.format("exceptionMessage[%d]", i),
        (Result result) -> result.getFailures().get(i).getMessage()
    );
  }

  private static final Function<Result, String> FIRST_EXCEPTION_MESSAGE = exceptionMessage(0);

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
    assertThat(
        JUnitCore.runClasses(UndefinedParameterReferenced.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$()
        ));
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
    assertThat(
        JUnitCore.runClasses(InvalidParameterSourceMethods.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(3).$(),
            asString(exceptionMessage(0)).containsString("'a' must not have any parameter").$(),
            asString(exceptionMessage(1)).containsString("'b' must be public").$(),
            asString(exceptionMessage(2)).containsString("'c' must not be static ").$()
        ));
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
    Crest.assertThat(
        JUnitCore.runClasses(InvalidConditionMethods.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(3).$(),
            asInteger("getFailureCount").equalTo(3).$(),
            asString(function(
                "1st failure", (Result result) -> result.getFailures().get(0).getMessage()
            )).containsString("'nonPublic' must be public").$(),
            asString(function(
                "2nd failure", (Result result) -> result.getFailures().get(1).getMessage()
            )).containsString("'staticMethod' must not be static").$(),
            asString(function(
                "3rd failure", (Result result) -> result.getFailures().get(2).getMessage()
            )).containsString("'wrongType' must return").$()
        ));
  }

  @Test
  public void givenNonAnnotatedParameterInTestMethod$whenRunTest$thenAppropriateExceptionIsThrown() {
    Crest.assertThat(
        JUnitCore.runClasses(ParameterWithoutFromAnnotation.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(1).$(),
            asObject(function(
                "firstException",
                (Result result) -> result.getFailures().get(0).getException()
            )).isInstanceOf(
                TestDefinitionException.class
            ).$(),
            asString(FIRST_EXCEPTION_MESSAGE).containsString(
                "@From"
            ).$(),
            asString(FIRST_EXCEPTION_MESSAGE).containsString(
                "testMethod"
            ).$()

        ));
  }

  @Test
  public void givenNoParameterTestClass$whenRunTest$thenTestDefinitionExceptionThrown() {
    Crest.assertThat(
        JUnitCore.runClasses(NoParameter.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asString(function(
                "firstExceptionMessage", (Result result) -> result.getFailures().get(0).getMessage()
            )).containsString("No parameter is found").$()
        ));
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
            asString(FIRST_EXCEPTION_MESSAGE).equalTo(
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
    Crest.assertThat(
        JUnitCore.runClasses(MissingParameter.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(1).$(),
            asString(
                FIRST_EXCEPTION_MESSAGE
            ).containsString(
                "Parameter(s) were not found: [parameter2] in tuple: {parameter1=hello}"
            ).$()
        ));
  }

  @Test
  public void unknownParameterInSeed() {
    Crest.assertThat(
        JUnitCore.runClasses(
            UnknownParameter.class
        ),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(1).$(),
            asString(
                FIRST_EXCEPTION_MESSAGE
            ).containsString(
                "[unknownParameter] in tuple: {parameter1=hello, parameter2=hello, unknownParameter=hello}"
            ).$()
        ));
  }

  @Test
  public void typeMismatchInSeed() {
    Crest.assertThat(
        JUnitCore.runClasses(
            TypeMismatch.class
        ),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(5).$(),
            asString(
                FIRST_EXCEPTION_MESSAGE
            ).containsString(
                "is not compatible with parameter 1 of 'test(String,String)'"
            ).$()
        )
    );
  }
}
