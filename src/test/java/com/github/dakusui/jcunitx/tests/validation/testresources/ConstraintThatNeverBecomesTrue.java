package com.github.dakusui.jcunitx.tests.validation.testresources;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class ConstraintThatNeverBecomesTrue {
  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a() {
    return SimpleParameter.Descriptor.of(asList(1, 2, 3));
  }

  @Condition(constraint = true)
  public boolean neverTrue(
      @From("a") int a
  ) {
    return false;
  }

  @Test
  public void test(@From("a") int a) {

  }
}
