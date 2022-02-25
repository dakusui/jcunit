package com.github.dakusui.jcunitx.tests.usecases;


import com.github.dakusui.jcunitx.tests.usecases.parametersource.SeparatedParameterSpaceExample;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import static com.github.dakusui.crest.Crest.*;

public class UseCasesTest {
  @Test
  public void separatedParameterSpace() {
    assertThat(
        JUnitCore.runClasses(SeparatedParameterSpaceExample.class),
        allOf(
            asBoolean("wasSuccessful").isTrue().$(),
            asInteger("getRunCount").equalTo(3).$()
        )
    );
  }
}
