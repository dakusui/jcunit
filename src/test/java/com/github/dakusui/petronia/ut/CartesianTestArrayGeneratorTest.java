package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import com.github.dakusui.jcunit.generators.CartesianTestArrayGenerator;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartesianTestArrayGeneratorTest extends TestArrayGeneratorTest {

  @Override
  protected TestArrayGenerator<String> createTestArrayGenerator() {
    return new CartesianTestArrayGenerator<String>();
  }

  public static void main(String... args) {
    CartesianTestArrayGenerator<String> cartesian = new CartesianTestArrayGenerator<String>();
    LinkedHashMap<String, Object[]> domains = new LinkedHashMap<String, Object[]>();
    domains.put("A", new Object[] { "a1", "a2" });
    domains.put("B", new Object[] { "b1", "b2" });
    cartesian.init(new Value[0], domains);

    for (Map<String, Object> values : cartesian) {
      System.out.println(values);
    }
  }
}
