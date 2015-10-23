package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.standardrunner.annotations.CustomTestCases;
import com.github.dakusui.jcunit.standardrunner.annotations.Generator;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.standardrunner.annotations.TupleGeneration;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.generators.Replayer;
import com.github.dakusui.jcunit.standardrunner.JCUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.github.dakusui.jcunit.standardrunner.TestCaseUtils.factor;
import static com.github.dakusui.jcunit.standardrunner.TestCaseUtils.newTestCase;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = Replayer.class,
        params = {
            @Arg("FailedOnly"),
            @Arg("src/test/resources")
        }
    )
)
public class GeophileReplayerTest extends GeophileTestBase {
  @SuppressWarnings("unchecked")
  @CustomTestCases
  public static Iterable<Tuple> assertionErrorTestCases() {
    return Arrays
        .asList(
            newTestCase(
                factor("duplicates", "EXCLUDE"),
                factor("X", 1.0), factor("Y", 2.0),
                factor("X_BITS", 30), factor("Y_BITS", 27),
                factor("indexForLeft", false),
                factor("indexForRight", false),
                factor("numBoxes", 1000),
                factor("boxWidth", 0.5), factor("boxHeight", 1.5)
            ),
            newTestCase(
                factor("duplicates", "EXCLUDE"),
                factor("X", 2.0), factor("Y", 1.0), factor("X_BITS", 30),
                factor("Y_BITS", 27),
                factor("indexForLeft", false),
                factor("indexForRight", false),
                factor("numBoxes", 1000), factor("boxWidth", 1.5),
                factor("boxHeight", 0.5)
            ));
  }

  @Test
  public void test() {
  }
}
