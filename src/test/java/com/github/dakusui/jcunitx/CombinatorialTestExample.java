package com.github.dakusui.jcunitx;

import com.github.dakusui.jcunitx.annotations.Combinatorial;
import com.github.dakusui.jcunitx.engine.junit5.compat.ValueSource;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;

public class CombinatorialTestExample {
  @Combinatorial
  @ValueSource(strings = { "", "  ", "\t", "\n" })
  void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings(String input) {
    Assertions.assertTrue(Strings.isBlank(input));
  }
}
