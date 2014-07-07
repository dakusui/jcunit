package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

public class SimpleTestArrayGeneratorTest extends TestArrayGeneratorTest {

  @Override
  protected TestArrayGenerator<String> createTestArrayGenerator() {
    return new SimpleTestArrayGenerator<String>();
  }
}
