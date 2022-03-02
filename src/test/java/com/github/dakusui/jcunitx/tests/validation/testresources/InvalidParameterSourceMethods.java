package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class InvalidParameterSourceMethods extends InvalidTestClass  {
  @SuppressWarnings("unused")
  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a(int a) {
    return SimpleParameter.Descriptor.of(asList(-1, 0, 1, 2, a));
  }

  @SuppressWarnings("unused")
  @ParameterSource
  private SimpleParameter.Descriptor<Integer> b() {
    return SimpleParameter.Descriptor.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @ParameterSource
  public static SimpleParameter.Descriptor<Integer> c() {
    return SimpleParameter.Descriptor.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @Test
  public void test(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
  }
}
