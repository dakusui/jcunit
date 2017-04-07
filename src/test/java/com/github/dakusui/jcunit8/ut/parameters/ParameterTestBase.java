package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestSuite;

import static java.util.Arrays.asList;

abstract class ParameterTestBase {

  TestSuite<Tuple> buildTestSuite(Parameter... parameters) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(asList(parameters))
        .build();
    return Pipeline.Standard
        .<Tuple>create()
        .execute(
            Config.Builder.forTuple(
                new Requirement.Builder().
                    withNegativeTestGeneration(false)
                    .build())
                .build(),
            parameterSpace
        );
  }
}
