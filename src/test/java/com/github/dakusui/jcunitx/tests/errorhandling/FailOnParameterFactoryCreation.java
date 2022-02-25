package com.github.dakusui.jcunitx.tests.errorhandling;

import com.github.dakusui.jcunitx.metamodel.parameters.Simple;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class FailOnParameterFactoryCreation extends JUnit4_13Workaround {
  public static final String INTENTIONAL_EXCEPTION_MESSAGE = "Intentional exception";

  @ParameterSource
  public Simple.Factory<Integer> a() throws Throwable {
    throw new RuntimeException(INTENTIONAL_EXCEPTION_MESSAGE);
  }

  @Test
  public void test(
      @From("a") int a
  ) {
  }
}
