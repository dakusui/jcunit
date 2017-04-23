package com.github.dakusui.jcunit8.tests.features.pipeline.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.testutils.PipelineTestBase;
import com.github.dakusui.jcunit8.testutils.TestSuiteUtils;
import org.junit.Test;

import java.util.Collections;

import static com.github.dakusui.jcunit8.testutils.UTUtils.sizeIs;
import static java.util.Arrays.asList;

public class ImpossibleConstraintTest extends PipelineTestBase {
  @Test
  public void test() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            asList(
                simpleParameterFactoryWithDefaultValues().create("simple1"),
                simpleParameterFactoryWithDefaultValues().create("simple2"),
                simpleParameterFactoryWithDefaultValues().create("simple3")
            ),
            Collections.singletonList(
                Constraint.create((Tuple tuple) -> {
                  return false;
                }, "simple1") // Never becomes true
            )
        ),
        matcher(
            sizeIs(name("==0", value -> value == 0))
        )
    );

  }
}
