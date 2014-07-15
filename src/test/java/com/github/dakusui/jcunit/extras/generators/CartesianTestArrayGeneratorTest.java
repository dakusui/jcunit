package com.github.dakusui.jcunit.extras.generators;

import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters.Value;
import com.github.dakusui.jcunit.compat.generators.CartesianTestArrayGenerator;
import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;

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
