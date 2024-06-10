package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.annotations.JCUnitParameter.Type;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * // @formatter:off
 * // @formatter:on
 */
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(TestRegex.ParameterSpace.class)
public class TestRegex {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter(type = Type.REGEX, args = "(scott|john)")
    public static List<ValueResolver<String>> param1() {
      return asList(
          ValueResolver.of("Hello, John").name("john"),
          ValueResolver.of("Howdy, Scott").name("scott"));
    }
  }

  @JCUnitTest
  public void testMethod(@From("param1") String param1) {
    System.out.println("param1:" + param1);
  }
}
