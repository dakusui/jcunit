package com.github.dakusui.jcunit8.tests.validation.testclassesundertest;

import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class ParameterWithoutFromAnnotation {
  @Test
  public void test(
      int a
  ) {

  }
}
