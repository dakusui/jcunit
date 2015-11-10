package com.github.dakusui.jcunit.tests.testutils;


import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UTUtilsTest {
  @Test
  public void generateAllPossibleTuples() {
    assertThat(
        new HashSet<Tuple>(
            UTUtils.defaultFactors.generateAllPossibleTuples(2)
        ),
        is(new HashSet<Tuple>(asList(new Tuple[] {
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").build()
        })))
    );
  }

  /**
   * A test to know system properties.
   * Remove '@Ignore` annotations temporarily when this method becomes necessary.
   */
  @Test
  @Ignore
  public void showSystemProperties() {
    System.out.println("== PROP ==");
    List<String> l = Utils.transform(System.getProperties().entrySet(), new Utils.Form<Map.Entry<Object, Object>, String>() {
      @Override
      public String apply(Map.Entry<Object, Object> in) {
        return in.getKey().toString();
      }
    });
    Collections.sort(l);
    for (String each : l) {
      System.out.printf("%s=%s%n", each, System.getProperty(each));
    }
  }

  /**
   * A test to know environment variables.
   * Remove '@Ignore` annotations temporarily when this method becomes necessary.
   */
  @Test
  @Ignore
  public void showEnvironmentVariables() {
    System.out.println("== ENV ==");
    List<String> l = Utils.transform(System.getenv().keySet(), new Utils.Form<String, String>() {
      @Override
      public String apply(String in) {
        return in;
      }
    });
    Collections.sort(l);
    for (String each : l) {
      System.out.printf("%s=%s%n", each, System.getProperty(each));
    }
  }
}
