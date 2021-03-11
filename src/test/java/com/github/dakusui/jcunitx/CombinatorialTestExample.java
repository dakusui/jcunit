package com.github.dakusui.jcunitx;

import com.github.dakusui.jcunitx.annotations.CombinatorialTest;
import com.github.dakusui.jcunitx.annotations.From;
import com.github.dakusui.jcunitx.annotations.ParameterSource;
import com.github.dakusui.jcunitx.engine.junit5.JCUnitExtension;
import com.github.dakusui.jcunitx.engine.junit5.compat.ValueSource;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

public class CombinatorialTestExample {
  //  @RegisterExtension
  //  static Extension extension = new JCUnitExtension();


  @ParameterSource
  public static void beforeAll() {
    System.out.println("beforeAll: Hello, world");
  }
  /*
  public static ParameterFactory input() {
    return Parameter.Simple.Factory.<Object>of(asList("hello", "world"));
  }

   */

  @CombinatorialTest()
  @ValueSource(strings = { "", "  ", "\t", "\n" })
  void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings1(@From("input") String input) {
    Assertions.assertTrue(Strings.isBlank(input));
  }

  @CombinatorialTest
  @ValueSource(strings = { "", "  ", "\t", "\n" })
  void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings2(@From("input") String input) {
    Assertions.assertTrue(Strings.isBlank(input));
  }
}
