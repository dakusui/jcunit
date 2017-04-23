package com.github.dakusui.jcunit8.tests.errorhandling;

import com.github.dakusui.jcunit8.testutils.ResultUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.jcunit8.testutils.UTBase.matcher;

public class ErrorHandlingTest {
  @Test
  public void failOnParameterFactoryCreation() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(FailOnParameterFactoryCreation.class),
        matcher(
            result -> !result.wasSuccessful(),
            result -> result.getFailureCount() == 1,
            result -> result.getFailures().get(0).getMessage().contains(FailOnParameterFactoryCreation.INTENTIONAL_EXCEPTION_MESSAGE)
        )
    );
  }
}
