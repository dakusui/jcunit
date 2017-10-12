package com.github.dakusui.jcunit8.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class SeedBase {
  @ParameterSource
  public Parameter.Simple.Factory<String> parameter1() {
    return Parameter.Simple.Factory.of(asList("value1", "value2"));
  }

  @ParameterSource
  public Parameter.Simple.Factory<String> parameter2() {
    return Parameter.Simple.Factory.of(asList("value1", "value2"));
  }

  @Test
  public void test(
      @SuppressWarnings("unused") @From("parameter1") String parameter1,
      @SuppressWarnings("unused") @From("parameter2") String parameter2
  ) {
  }
}
