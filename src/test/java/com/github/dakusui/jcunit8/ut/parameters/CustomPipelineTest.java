package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CustomPipelineTest extends PipelineTestBase {

  @Test
  public void whenBuildFactorSpace() {
    FactorSpace factorSpace = customParameterFactory().create("custom1").toFactorSpace();
    factorSpace.getFactors().forEach(System.out::println);
    factorSpace.getConstraints().forEach(System.out::println);
  }

  @Test
  public void whenGenerateSchemafulTupleSet() {
    Parameter parameter = customParameterFactory().create("custom1");
    FactorSpace factorSpace = parameter.toFactorSpace();
    Requirement requirement = new Requirement.Builder().build();

    SchemafulTupleSet schemafulTupleSet = SchemafulTupleSet.fromTuples(new IpoG(emptyList(), factorSpace, requirement).generate());
    schemafulTupleSet.forEach(new Consumer<Tuple>() {
      int i = 0;

      @Override
      public void accept(Tuple tuple) {
        System.out.println(i++ + ":" + parameter.composeValueFrom(tuple) + ":" + tuple);
      }
    });
  }

  @Test
  public void whenBuildTestSuite() {
    for (TestCase<Tuple> each : generateTestSuite(
        customParameterFactory().create("custom1")
    )) {
      System.out.println(each.get());
    }
  }

  @Test
  public void whenBuildTestSuite2() {
    for (TestCase<Tuple> each : generateTestSuite(
        customParameterFactory().create("custom1"),
        simpleParameterFactoryWithDefaultValues().create("simple1")
    )) {
      System.out.println(each.get());
    }
  }

  @Test
  public void whenBuildTestSuite3() {
    for (TestCase<Tuple> each : generateTestSuite(
        simpleParameterFactoryWithDefaultValues().create("simple1"),
        simpleParameterFactoryWithDefaultValues().create("simple2"),
        simpleParameterFactoryWithDefaultValues().create("simple3")
    )) {
      System.out.println(each.get());
    }
  }

  @Test
  public void whenPreprocess$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpace parameterSpace = preprocess(customParameterFactory().create("custom1"));
    parameterSpace.getParameterNames().forEach(
        parameterName -> System.out.println(parameterSpace.getParameter(parameterName))
    );
    parameterSpace.getConstraints().forEach(
        System.out::println
    );
  }

  @Test
  public void whenPreprocessWithConstraints$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpace parameterSpace = preprocess(
        singletonList(customParameterFactory().create("custom1")),
        singletonList(new Constraint() {
          @Override
          public boolean test(Tuple tuple) {
            return true;
          }

          @Override
          public List<String> involvedKeys() {
            return singletonList("custom1");
          }
        }));
    parameterSpace.getParameterNames().forEach(
        parameterName -> System.out.println(parameterSpace.getParameter(parameterName))
    );
    parameterSpace.getConstraints().forEach(
        System.out::println
    );
  }

  @Test
  public void whenEngine$thenSchemafulTupleSetGenerated() {
    engine(
        singletonList(customParameterFactory().create("custom1")),
        emptyList()
    ).forEach(System.out::println);
  }


  @Test
  public void whenEngine2$thenSchemafulTupleSetGenerated() {
    engine(
        asList(customParameterFactory().create("custom1"), simpleParameterFactoryWithDefaultValues().create("simple1")),
        emptyList()
    ).forEach(System.out::println);
  }
}
