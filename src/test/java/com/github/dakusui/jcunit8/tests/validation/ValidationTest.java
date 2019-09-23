package com.github.dakusui.jcunit8.tests.validation;

import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.tests.validation.testresources.*;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.MissingParameter;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.TypeMismatch;
import com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature.UnknownParameter;
import com.github.dakusui.jcunit8.testutils.JUnit4TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class ValidationTest {
  private static Function<Result, String> exceptionMessageAt(int i) {
    return function(
        format("exceptionMessageAt[%d]", i),
        (Result result) -> result.getFailures().get(i).getMessage()
    );
  }

  @Test
  public void givenUndefinedParameterReferenced$whenRunTestClass$thenAppropriateExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(UndefinedParameterReferenced.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(1).$(),
            asListOf(
                String.class,
                function(
                    "failureMessages",
                    (Result result) -> result.getFailures().stream().map(
                        Failure::getMessage
                    ).collect(
                        toList()
                    )
                )
            ).allMatch(
                predicate(
                    format(
                        "contains['a', '%s', '%s']", ParameterSource.class.getSimpleName(), UndefinedParameterReferenced.class.getSimpleName()
                    ),
                    s -> s.contains("'a'") &&
                        s.contains(ParameterSource.class.getSimpleName()) &&
                        s.contains(UndefinedParameterReferenced.class.getSimpleName())
                )).$()
        ));
  }

  @Test
  public void givenOverloadedParameterSourceMethod$whenRunTestClass$thenAppropriateExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(ParameterSourceOverloaded.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(1).$(),
            asListOf(
                String.class,
                function(
                    "failureMessages",
                    (Result result) -> result.getFailures().stream().map(Failure::getMessage).collect(toList())
                )
            ).anyMatch(
                predicate(
                    "contains['a', 'must not have any parameter']",
                    s -> s.contains("'a'") && s.contains("must not have any parameter")
                )
            ).$()

        ));
  }

  @Test
  public void givenInvalidParameterSourceMethods$whenRunTestClass$thenAppropriateExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(InvalidParameterSourceMethods.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(3).$(),
            asString(exceptionMessageAt(0)).containsString("'a' must not have any parameter").$(),
            asString(exceptionMessageAt(1)).containsString("'b' must be public").$(),
            asString(exceptionMessageAt(2)).containsString("'c' must not be static ").$()
        ));
  }

  @Test
  public void givenInvalidReferencesToConstraints$whenRunTestClass$thenAppropriateExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(UndefinedConstraint.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(2).$(),
            asString(exceptionMessageAt(0)).containsString("'undefinedConstraint' was not found").$(),
            asString(exceptionMessageAt(1)).containsString("'malformedConstraint!' is not a valid condition name").$()
        )
    );
  }

  @Test
  public void givenInvalidConditions$whenRunTestClass$thenAppropriateExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(InvalidConditionMethods.class),
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
    assertThat(
        JUnit4TestUtils.runClasses(ParameterWithoutFromAnnotation.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(1).$(),
            asObject(function(
                "firstException",
                (Result result) -> result.getFailures().get(0).getException()
            )).isInstanceOf(
                TestDefinitionException.class
            ).$(),
            asString(exceptionMessageAt(0)).containsString(
                "@From"
            ).$(),
            asString(exceptionMessageAt(0)).containsString(
                "testMethod"
            ).$()

        ));
  }

  @Test
  public void givenNoParameterTestClass$whenRunTest$thenTestDefinitionExceptionThrown() {
    assertThat(
        JUnit4TestUtils.runClasses(NoParameter.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asString(function(
                "firstExceptionMessage", (Result result) -> result.getFailures().get(0).getMessage()
            )).containsString("No parameter is found").$()
        ));
  }


  @Test
  public void typeCompatibilityTest1() {
    assertThat(
        JUnit4TestUtils.runClasses(IncompatibleParameters.IncompatibleType.class),
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
    assertThat(
        JUnit4TestUtils.runClasses(IncompatibleParameters.CompatibleNullValue.class),
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
    assertThat(
        JUnit4TestUtils.runClasses(IncompatibleParameters.IncompatiblePrimitiveType.class),
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
            asString(exceptionMessageAt(0)).equalTo(
                "'1' is not compatible with parameter 0 of 'testMethod(boolean)'"
            ).$()
        )
    );
  }

  @Test
  public void typeCompatibilityTest4() {
    assertThat(
        JUnit4TestUtils.runClasses(IncompatibleParameters.IncompatibleNullValue.class),
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
    Result result = JUnit4TestUtils.runClasses(NoTestMethod.class);
    Assert.assertThat(
        result.getFailures().get(0).getMessage(),
        CoreMatchers.containsString("No runnable methods")
    );
  }

  @Test
  public void missingParameterInSeed() {
    assertThat(
        JUnit4TestUtils.runClasses(MissingParameter.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(1).$(),
            asString(
                exceptionMessageAt(0)
            ).containsString(
                "Parameter(s) were not found: [parameter2] in tuple: {parameter1=hello}"
            ).$()
        ));
  }

  @Test
  public void unknownParameterInSeed() {
    assertThat(
        JUnit4TestUtils.runClasses(
            UnknownParameter.class
        ),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(1).$(),
            asString(
                exceptionMessageAt(0)
            ).containsString(
                "[unknownParameter] in tuple: {parameter1=hello, parameter2=hello, unknownParameter=hello}"
            ).$()
        ));
  }

  @Test
  public void typeMismatchInSeed() {
    assertThat(
        JUnit4TestUtils.runClasses(
            TypeMismatch.class
        ),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").equalTo(5).$(),
            asString(
                exceptionMessageAt(0)
            ).containsString(
                "is not compatible with parameter 1 of 'test(String,String)'"
            ).$()
        )
    );
  }
}
