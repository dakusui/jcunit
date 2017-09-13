package com.github.dakusui.jcunit8.examples.parameterizedconstraint;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class ParameterizedConstraintExample {
  @ParameterSource
  public Parameter.Factory a() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @ParameterSource
  public Parameter.Factory b() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @ParameterSource
  public Parameter.Factory c() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @Condition
  public boolean greaterThan(@From("@arg") int value, @From("@arg") String strThreshold) {
    return value > Integer.parseInt(strThreshold);
  }

  @Test
  @Given("greaterThan @a 2")
  public void whenRunTest$thenWorks(@From("a") int a, @From("b") int b, @From("c") int c) {
    System.out.printf("runTest:(a,b,c)=(%s,%s,%s)", a, b, c);
  }
}
