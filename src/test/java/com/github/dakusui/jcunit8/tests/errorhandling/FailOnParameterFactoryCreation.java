package com.github.dakusui.jcunit8.tests.errorhandling;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class FailOnParameterFactoryCreation {
  public static final String INTENTIONAL_EXCEPTION_MESSAGE = "Intentional exception";

  @ParameterSource
  public Parameter.Simple.Factory<Integer> a() throws Throwable {
    throw new RuntimeException(INTENTIONAL_EXCEPTION_MESSAGE);
  }

  @Test
  public void test(
      @From("a") int a
  ) {
  }
}
