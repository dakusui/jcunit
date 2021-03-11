package com.github.dakusui.jcunitx;

import com.github.dakusui.jcunitx.annotations.CombinatorialTest;
import com.github.dakusui.jcunitx.annotations.From;
import com.github.dakusui.jcunitx.engine.junit5.JCUnitExtension;
import com.github.dakusui.jcunitx.engine.junit5.compat.ValueSource;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;

public class CombinatorialNestedTestExample {
  @Nested
  public class NestedTestClass1 {
    @CombinatorialTest
    @ValueSource(strings = { "", "  ", "\t", "\n" })
    void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings1a(@From("input") String input) {
      Assertions.assertTrue(Strings.isBlank(input));
    }

    @CombinatorialTest
    @ValueSource(strings = { "", "  ", "\t", "\n" })
    void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings1b(@From("input") String input) {
      Assertions.assertTrue(Strings.isBlank(input));
    }
  }

  @Nested
  public class NestedTestClass2 {
    @CombinatorialTest
    @ValueSource(strings = { "", "  ", "\t", "\n" })
    void isBlank_ShouldReturnTrueForAllTypesOfBlankStrings2(@From("input") String input) {
      Assertions.assertTrue(Strings.isBlank(input));
    }
  }
}
