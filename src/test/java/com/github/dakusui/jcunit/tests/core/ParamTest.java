package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.standardrunner.annotations.Param;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ParamTest {
  @Test
  public void test() {
    Param[] params = new Param.ArrayBuilder()
        .add("Hello", "world")
        .add("Hello!", "world!")
        .build();
    assertEquals("Hello", params[0].value()[0]);
    assertEquals("world", params[0].value()[1]);
    assertEquals("Hello!", params[1].value()[0]);
    assertEquals("world!", params[1].value()[1]);
  }
}
