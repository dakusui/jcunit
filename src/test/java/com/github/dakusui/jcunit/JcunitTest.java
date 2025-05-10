package com.github.dakusui.jcunit;

import com.github.dakusui.jcunit.core.cfg.TermElement;
import org.junit.Test;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;

public class JcunitTest {
  @Test
  public void testJcunit() {
    System.out.println("Hello, Jcunit!");
  }

  @Test
  public void testTermElement() {
    TermElement termElement = TermElement.create("hi");
    assertStatement(value(termElement.value()).asObject().toBe().notNull());
  }
}
