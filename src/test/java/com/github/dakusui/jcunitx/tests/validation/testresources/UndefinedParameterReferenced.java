package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.Simple;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class UndefinedParameterReferenced extends InvalidTestClass {
  /*
   * This method is hidden and errors should be reported.
   */
  // @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @Test
  public void performScenario1(
      @From("a") int a
  ) {
  }
}
