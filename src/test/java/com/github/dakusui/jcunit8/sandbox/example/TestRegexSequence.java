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
@UsingParameterSpace(TestRegexSequence.ParameterSpace.class)
public class TestRegexSequence {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter(type = Type.REGEX, args = {"(scott|john)", "(tiger|doe)"})
    public static List<ValueResolver<String>> param1() {
      return asList(
          ValueResolver.of("Hello, John").name("john"),
          ValueResolver.of("Howdy, Scott").name("scott"),
          ValueResolver.of("Hello, Mr. Tiger").name("tiger"),
          ValueResolver.of("Howdy, Mr. Doe").name("doe")
          );
    }
  }

  @JCUnitTest
  public void testMethod(@From(value = "param1", range = "0") String param1, @From(value = "param1", range = "1") String param2) {
    System.out.println("param1:" + param1 + ";param2:" + param2);
  }
}
