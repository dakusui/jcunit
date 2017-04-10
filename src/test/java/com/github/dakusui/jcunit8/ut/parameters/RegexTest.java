package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.function.Consumer;

public class RegexTest extends ParameterTestBase {
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

    SchemafulTupleSet schemafulTupleSet = SchemafulTupleSet.fromTuples(new IpoG(Collections.emptyList(), factorSpace, requirement).generate());
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
    for (TestCase<Tuple> each : buildTestSuite(
        Parameter.Regex.Factory.of("A(B|C){0,5}").create("regex1")
    )) {
      System.out.println(each.get());
    }
  }
}
