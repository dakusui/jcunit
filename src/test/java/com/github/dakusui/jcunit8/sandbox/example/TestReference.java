package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.factorspace.Range;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.jcunit.core.Invokable.referenceTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off
 * // @formatter:on
 */
@Disabled
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(TestReference.ParameterSpace.class)
public class TestReference {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param1() {
      return asList(
          ValueResolver.of("John"),
          ValueResolver.fromInvokable(referenceTo("param3", Range.of("0"))));
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param3() {
      return singletonList(ValueResolver.of("Scott"));
    }
  }

  @JCUnitTest
  public void testMethod(@From("param1") String param1) {
    System.out.println("param1:" + param1);
  }
}
