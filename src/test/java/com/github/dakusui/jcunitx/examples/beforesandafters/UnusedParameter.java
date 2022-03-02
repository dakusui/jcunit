package com.github.dakusui.jcunitx.examples.beforesandafters;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class UnusedParameter extends JUnit4_13Workaround {
  @ParameterSource
  public Parameter.Descriptor used() {
    return ParameterUtils.simple(true, false);
  }

  @ParameterSource
  public Parameter.Descriptor notReferednced() {
    return ParameterUtils.simple("Z1", "Z2");
  }

  @Test
  public void test(@From("used") boolean parameter) {
    System.out.println(parameter);
  }
}
