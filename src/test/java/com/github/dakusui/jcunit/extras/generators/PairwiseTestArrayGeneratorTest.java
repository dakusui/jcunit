package com.github.dakusui.jcunit.extras.generators;

import com.github.dakusui.jcunit.compat.generators.PairwiseTestArrayGenerator;
import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;

public class PairwiseTestArrayGeneratorTest extends TestArrayGeneratorTest {
  @Override
  protected TestArrayGenerator<String> createTestArrayGenerator() {
    return new PairwiseTestArrayGenerator<String>();
  }
}
