package com.github.dakusui.jcunitx.examples.parameterizedconstraint;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Given;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

// This is an example supposed to be executed by another class during the "test" lifecycle of maven.
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class ParameterizedConstraintExample extends JUnit4_13Workaround {
  @ParameterSource
  public Parameter.Descriptor<?> a() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @ParameterSource
  public Parameter.Descriptor<?> b() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @ParameterSource
  public Parameter.Descriptor<?> c() {
    return ParameterUtils.simple(1, 2, 3);
  }

  @Condition
  public boolean isOneOf(@From("@arg") int value, @From("@arg") Object... strThreshold) {
    return Arrays.asList(strThreshold).contains(Integer.toString(value));
  }

  @Condition
  public boolean littleThan(@From("@arg") int value, @From("@arg") String strThreshold) {
    return value < Integer.parseInt(strThreshold);
  }

  @Test
  @Given(value = "isOneOf @a 2 3&&littleThan @b 2")
  public void whenRunTest$thenWorks(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    System.out.printf("runTest:(a,b,c)=(%s,%s,%s)", a, b, c);
  }

}
