package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

// This is an example class executed by another test class during the "test" life cycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class NoTestMethod extends JUnit4_13Workaround {
  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a() {
    return SimpleParameter.Descriptor.of(asList(-1, 0, 1, 2, 4));
  }
}
