package com.github.dakusui.jcunit8.tests.errorhandling;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.github.dakusui.crest.Crest.*;

public class ErrorHandlingTest {
  @Test
  public void failOnParameterFactoryCreation() {
    assertThat(
        JUnitCore.runClasses(FailOnParameterFactoryCreation.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getFailureCount").equalTo(1).$(),
            asString(function(
                "firstExceptionMessage", (Result result) -> result.getFailures().get(0).getMessage())
            ).containsString(
                FailOnParameterFactoryCreation.INTENTIONAL_EXCEPTION_MESSAGE
            ).$()
        )
    );
  }
}
