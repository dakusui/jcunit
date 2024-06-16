package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.annotations.JCUnitParameter.Type;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * // @formatter:off
 * // @formatter:on
 */
@ExtendWith(JCUnitTestEngine.class)
@ConfigurePipelineWith(parameterSpaceSpecClass = TestRegexSequence.ParameterSpace.class)
public class TestRegexSequence {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter(type = Type.REGEX, args = {"(scott|john|jane)", "(tiger|(doe{1,2})){1,2}"})
    public static List<ValueResolver<String>> param1() {
      return asList(
          ValueResolver.of("Hello, John").name("john"),
          ValueResolver.of("Howdy, Scott").name("scott"),
          ValueResolver.of("Hello, Mr. Tiger").name("tiger"),
          ValueResolver.of("Howdy, Mr. Doe").name("doe"),
          ValueResolver.of("Hi, Ms. Doe").name("jane"));
    }
  }

  @JCUnitTest
  public void testMethod(@From(value = "param1", index = 0) String param1, @From(value = "param1", index = 1) String param2) {
    System.out.println("param1:" + param1 + ";param2:" + param2);
  }

  @JCUnitTest
  public void testMethod2(@From(value = "param1", index = From.ALL) List<String> param1) {
    System.out.println("param1:" + String.join("-", param1));
  }
}
