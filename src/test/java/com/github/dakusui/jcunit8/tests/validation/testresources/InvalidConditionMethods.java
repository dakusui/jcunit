package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.metamodel.parameters.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class InvalidConditionMethods extends JUnit4_13Workaround {
  @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(1, 2, 3));
  }

  @Condition
  boolean nonPublic() {
    return false;
  }

  @Condition
  public static boolean staticMethod() {
    return false;
  }

  @Condition
  public int wrongType() {
    return 0;
  }

  @Test
  public void test(@From("a") int a) {
  }
}
