package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class UndefinedParameterReferenced extends InvalidTestClass {
  /*
   * This method is hidden and errors should be reported.
   */
  // @ParameterSource
  public Parameter.Simple.Factory<Integer> a() {
    return Parameter.Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @Test
  public void performScenario1(
      @From("a") int a
  ) {
  }
}
