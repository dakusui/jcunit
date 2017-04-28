package com.github.dakusui.jcunit8.tests.errorhandling;

import com.github.dakusui.jcunit8.testutils.ResultUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcherFromPredicates;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;

public class ErrorHandlingTest {
  @Test
  public void failOnParameterFactoryCreation() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(FailOnParameterFactoryCreation.class),
        matcherFromPredicates(
            oracle("was not successful", result -> !result.wasSuccessful()),
            oracle("failure count == 1", result -> result.getFailureCount() == 1),
            oracle("intentional exception", result -> result.getFailures().get(0).getMessage().contains(FailOnParameterFactoryCreation.INTENTIONAL_EXCEPTION_MESSAGE))
        )
    );
  }
}
