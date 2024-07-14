package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.factorspace.Range;
import com.github.jcunit.annotations.JCUnitParameter.Type;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.jcunit.core.Invokable.referenceTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(TestRegexWithReference.ParameterSpace.class)
public class TestRegexWithReference {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter(type = Type.REGEX, args = "(scott|john)")
    public static List<ValueResolver<String>> param1() {
      return asList(
          ValueResolver.of("John").name("john"),
          ValueResolver.<String>fromInvokable(referenceTo("param3", Range.of("0"))).name("scott"));
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param3() {
      return singletonList(ValueResolver.of("Scott"));
    }
  }


  @JCUnitTest
  public void testMethod(@From("param1") String
 param1) {
    System.out.println("param1:" + param1);
  }
}
