package com.github.dakusui.jcunit8.examples.beforesandafters;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class UnusedParameter extends JUnit4_13Workaround {
  @ParameterSource
  public Parameter.Factory used() {
    return ParameterUtils.simple(true, false);
  }

  @ParameterSource
  public Parameter.Factory notReferednced() {
    return ParameterUtils.simple("Z1", "Z2");
  }

  @Test
  public void test(@From("used") boolean parameter) {
    System.out.println(parameter);
  }
}
