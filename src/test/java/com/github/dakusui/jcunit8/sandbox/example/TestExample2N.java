package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.ConfigureWith;
import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Given;
import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

@ExtendWith(JCUnitTestEngine.class)
@ConfigureWith(parameterSpace = TestExample2.ParameterSpace.class,
    pipelineArguments = {
        @ConfigureWith.Entry(name = "strength", value = "2"),
        @ConfigureWith.Entry(name = "negativeTestGeneration", value = "true"),
        @ConfigureWith.Entry(name = "seedGeneratorMethod", value = "seeds")
    })
public class TestExample2N {
  @JCUnitTest
  @Given("startingWithSalute")
  public void testMethod(@From("param1") String param1, @From("param2") Map<String, List<String>> param2) {
    System.out.println("param1:" + param1 + ", param2:" + param2);
  }

}
