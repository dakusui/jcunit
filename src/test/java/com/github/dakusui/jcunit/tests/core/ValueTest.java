package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ValueTest {
  @Test
  public void test() {
    Value[] values = new Value.ArrayBuilder()
        .add("Hello", "world")
        .add("Hello!", "world!")
        .build();
    assertEquals("Hello", values[0].value()[0]);
    assertEquals("world", values[0].value()[1]);
    assertEquals("Hello!", values[1].value()[0]);
    assertEquals("world!", values[1].value()[1]);
  }
}
