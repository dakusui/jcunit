package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArgTest {
  @Test
  public void test() {
    Arg[] args = new Arg.ArrayBuilder()
        .add("Hello", "world")
        .add("Hello!", "world!")
        .build();
    assertEquals("Hello", args[0].value()[0]);
    assertEquals("world", args[0].value()[1]);
    assertEquals("Hello!", args[1].value()[0]);
    assertEquals("world!", args[1].value()[1]);
  }
}
