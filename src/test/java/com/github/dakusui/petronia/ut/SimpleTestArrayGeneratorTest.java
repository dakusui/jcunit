package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.enumerator.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.enumerator.TestArrayGenerator;

public class SimpleTestArrayGeneratorTest extends TestArrayGeneratorTest {

	@Override
	protected TestArrayGenerator<String, String> createTestArrayGenerator() {
		return new SimpleTestArrayGenerator<String, String>();
	}
}
