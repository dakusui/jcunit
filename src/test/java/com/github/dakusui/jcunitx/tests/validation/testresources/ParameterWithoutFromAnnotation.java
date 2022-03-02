package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class ParameterWithoutFromAnnotation extends JUnit4_13Workaround {
  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a() {
    return SimpleParameter.Descriptor.of(asList(0, 1));
  }

  @SuppressWarnings("unused")
  @Test
  public void testMethod(
      int a // Used in test
  ) {
  }
}
