package com.github.dakusui.jcunit8.tests.usecases;


import com.github.dakusui.jcunit8.tests.usecases.parametersource.SeparatedParameterSpaceExample;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class UseCasesTest {
  @Test
  public void separatedParameterSpace() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(SeparatedParameterSpaceExample.class),
        UTUtils.matcherFromPredicates(
            Result::wasSuccessful,
            result -> result.getRunCount() == 3
        ));
  }
}
