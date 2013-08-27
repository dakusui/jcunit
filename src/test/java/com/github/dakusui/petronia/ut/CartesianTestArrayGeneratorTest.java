package com.github.dakusui.petronia.ut;

import java.util.Map;
import java.util.TreeMap;

import com.github.dakusui.jcunit.enumerator.CartesianTestArrayGenerator;
import com.github.dakusui.jcunit.enumerator.TestArrayGenerator;

public class CartesianTestArrayGeneratorTest extends TestArrayGeneratorTest {

	@Override
	protected TestArrayGenerator<String, String> createTestArrayGenerator() {
		return new CartesianTestArrayGenerator<String, String>();
	}
	
	public static void main(String... args) {
		CartesianTestArrayGenerator<String, String> cartesian = new CartesianTestArrayGenerator<String, String>();
		Map<String, String[]> domains = new TreeMap<String, String[]>();
		domains.put("A", new String[]{"a1", "a2"});
		domains.put("B", new String[]{"b1", "b2"});
		cartesian.init(domains);
		
		for (Map<String, String> values : cartesian) {
			System.out.println(values);
		}
	}
}