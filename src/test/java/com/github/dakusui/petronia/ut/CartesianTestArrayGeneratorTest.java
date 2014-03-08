package com.github.dakusui.petronia.ut;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.generators.CartesianTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

public class CartesianTestArrayGeneratorTest extends TestArrayGeneratorTest {

  @Override
  protected TestArrayGenerator<String, String> createTestArrayGenerator() {
    return new CartesianTestArrayGenerator<String, String>();
  }

  public static void main(String... args) {
    CartesianTestArrayGenerator<String, String> cartesian = new CartesianTestArrayGenerator<String, String>();
    LinkedHashMap<String, String[]> domains = new LinkedHashMap<String, String[]>();
    domains.put("A", new String[] { "a1", "a2" });
    domains.put("B", new String[] { "b1", "b2" });
    cartesian.init(new Value[0], domains);

    for (Map<String, String> values : cartesian) {
      System.out.println(values);
    }
  }
}
