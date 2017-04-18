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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class RegexTest extends PipelineTestBase {
  @Test
  public void whenBuildFactorSpace() {
    FactorSpace factorSpace = Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1").toFactorSpace();
    factorSpace.getFactors().forEach(System.out::println);
    factorSpace.getConstraints().forEach(System.out::println);
  }

  @Test
  public void whenGenerateSchemafulTupleSet() {
    Parameter parameter = Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1");
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
        Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1")
    )) {
      System.out.println(each.get());
    }
  }

  @Test
  public void whenPreprocess$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpace parameterSpace = preprocess(Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1"));
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
        singletonList(Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1")),
        singletonList(new Constraint() {
          @Override
          public boolean test(Tuple tuple) {
            return true;
          }

          @Override
          public List<String> involvedKeys() {
            return singletonList("regex1");
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
        singletonList(Parameter.Regex.Factory.of("A(B|C){0,3}").create("regex1")),
        emptyList()
        ).forEach(System.out::println);
  }

  @Test
  public void whenEncode$thenFactorSpaceCreated() {
    FactorSpace factorSpace = encode(
        singletonList(Parameter.Regex.Factory.of("A((B|C)D){0,3}").create("regex1")),
        emptyList()
    );
    System.out.println("factors");
    factorSpace.getFactors().forEach(
        System.out::println
    );
    System.out.println("constraints");
    factorSpace.getConstraints().forEach(
        System.out::println
    );
  }
}
