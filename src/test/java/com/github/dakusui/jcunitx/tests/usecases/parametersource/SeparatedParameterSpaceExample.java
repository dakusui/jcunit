package com.github.dakusui.jcunitx.tests.usecases.parametersource;

import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import org.junit.Test;
import org.junit.runner.RunWith;


// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
@ConfigureWith(parameterSpace = SeparatedParameterSource.class)
public class SeparatedParameterSpaceExample extends JUnit4_13Workaround {
  @Test
  public void test(
      @From("a") int a
  ) {
    System.out.println(a);
  }
}
