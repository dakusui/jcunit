package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.plugins.Replayer;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static com.github.dakusui.jcunit.runners.standard.TestCaseUtils.factor;
import static com.github.dakusui.jcunit.runners.standard.TestCaseUtils.newTestCase;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(
        value = Replayer.class,
        configValues = {
            @Value({ "com.github.dakusui.jcunit.tests.bugfixes.geophile.NullCoveringArrayEngine" }),
            @Value("Replay"),
            @Value("All")
        }
    )
)
public class GeophileReplayerExample extends GeophileTestBase {
  public GeophileReplayerExample() {
    super(38, 0, 0);
  }

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

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
    UTUtils.stdout().println(TestCaseUtils.toTestCase(this));
  }
}
