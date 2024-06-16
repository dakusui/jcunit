package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.annotations.ConfigureWith.Entry;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.jcunit.annotations.JCUnitCondition.Type.CONSTRAINT;
import static java.util.Arrays.asList;

@ExtendWith(JCUnitTestEngine.class)
@ConfigureWith(
    parameterSpace = TestExample5.ParameterSpace.class,
    pipelineArguments = {
        @Entry(name = "strength", value = "2"),
        @Entry(name = "negativeTestGeneration", value = "true"),
        @Entry(name = "seedGeneratorMethod", value = "seeds")
    })
public class TestExample5 {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param1() {
      return asList(ValueResolver.from("X1").$(),
                    ValueResolver.from("X2").$());
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param2() {
      return asList(ValueResolver.of("Y1"),
                    ValueResolver.of("Y2"));
    }

    @Named
    @JCUnitCondition(CONSTRAINT)
    public static boolean aConstraint(@From("param1") String param1, @From("param2") String param2) {
      return !param1.equals("X1") || param2.equals("Y1");
    }
  }

  @JCUnitTest
  public void testMethod(@From("param1") String param1, @From("param2") String param2) {
    System.out.println("param1:" + param1 + ",param2:" + param2);
  }

  @JCUnitTest
  @Given("!aConstraint")
  public void testMethodNegative(@From("param1") String param1, @From("param2") String param2) {
    System.out.println("param1:" + param1 + ",param2:" + param2);
  }
}
