package com.github.dakusui.jcunit.framework.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.generators.RecordedTuplePlayer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.Arrays;

import static com.github.dakusui.jcunit.core.TestCaseUtils.*;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = RecordedTuplePlayer.class,
        params = {
            @Param("FailedOnly"),
            @Param("src/test/resources")
        }
    )
)
public class GeophileReplayerTest extends GeophileTestBase {
  @SuppressWarnings("unchecked")
  @CustomTestCases
  public static Iterable<LabeledTestCase> assertionErrorTestCases() {
    return Arrays
        .asList(createLabeledTestCase(Arrays.<Serializable>asList("Normal"),
                newTestCase(
                    factor("duplicates", "EXCLUDE"),
                    factor("X", 1.0), factor("Y", 2.0),
                    factor("X_BITS", 30), factor("Y_BITS", 27),
                    factor("indexForLeft", false),
                    factor("indexForRight", false),
                    factor("numBoxes", 1000),
                    factor("boxWidth", 0.5), factor("boxHeight", 1.5)
                )
            ),
            createLabeledTestCase(Arrays.<Serializable>asList("Normal"),
                newTestCase(
                    factor("duplicates", "EXCLUDE"),
                    factor("X", 2.0), factor("Y", 1.0), factor("X_BITS", 30),
                    factor("Y_BITS", 27),
                    factor("indexForLeft", false),
                    factor("indexForRight", false),
                    factor("numBoxes", 1000), factor("boxWidth", 1.5),
                    factor("boxHeight", 0.5)
                )
            ));
  }

  @Test
  public void test() {
  }
}
