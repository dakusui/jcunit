package com.github.dakusui.jcunit8.tests.usecases;


import com.github.dakusui.jcunit8.tests.usecases.parametersource.SeparatedParameterSpaceExample;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.github.dakusui.jcunit8.testutils.UTBase.matcher;

public class UseCasesTest {
  @Test
  public void separatedParameterSpace() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(SeparatedParameterSpaceExample.class),
        matcher(
            Result::wasSuccessful,
            result -> result.getRunCount() == 3
        ));
  }
}
