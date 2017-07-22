package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class UndefinedConstraint extends InvalidTestClass {
  @SuppressWarnings("unused")
  @ParameterSource
  public Parameter.Simple.Factory<Integer> a() {
    return Parameter.Simple.Factory.of(asList(1,2,3));
  }

  @SuppressWarnings("unused")
  @Given("undefinedConstraint")
  @Test
  public void test1(
      @From("a") int a
  ) {
  }

  @SuppressWarnings("unused")
  @Given("malformedConstraint!")
  @Test
  public void test2(
      @From("a") int a
  ) {

  }
}
