package com.github.dakusui.jcunitx.tests.validation.testresources.seedfeature;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public abstract class SeedBase extends JUnit4_13Workaround {
  @ParameterSource
  public SimpleParameter.Descriptor<String> parameter1() {
    return SimpleParameter.Descriptor.of(asList("value1", "value2"));
  }

  @ParameterSource
  public SimpleParameter.Descriptor<String> parameter2() {
    return SimpleParameter.Descriptor.of(asList("value1", "value2"));
  }

  @Test
  public void test(
      @SuppressWarnings("unused") @From("parameter1") String parameter1,
      @SuppressWarnings("unused") @From("parameter2") String parameter2
  ) {
  }
}
