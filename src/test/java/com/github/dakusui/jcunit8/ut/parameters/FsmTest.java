package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterSpec;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.*;

public class FsmTest extends PipelineTestBase {
  @Test
  public void whenBuildFactorSpace() {
    FactorSpace factorSpace = Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 2).create("fsm1").toFactorSpace();
    factorSpace.getFactors().forEach(System.out::println);
    factorSpace.getConstraints().stream().map(TestPredicate::asString).forEach(System.out::println);
  }

  @Test
  public void whenGenerateSchemafulTupleSet() {
    Parameter<Scenario<FlyingSpaghettiMonster>> parameter = Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1");
    FactorSpace factorSpace = parameter.toFactorSpace();
    Requirement requirement = new Requirement.Builder().build();

    SchemafulTupleSet schemafulTupleSet = SchemafulTupleSet.fromTuples(new Cartesian(emptyList(), factorSpace, requirement).generate());
    schemafulTupleSet.forEach(new Consumer<Tuple>() {
      int i = 0;

      @Override
      public void accept(Tuple tuple) {
        System.out.println(i++ + ":" + parameter.composeValueFrom(tuple));
      }
    });
  }

  @Test
  public void whenGenerateTestSuite() {
    for (TestCase<Tuple> each : generateTestSuite(
        Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")
    )) {
      System.out.println(each.get());
    }
  }


  @Test
  public void whenGenerateTestSuiteFromFactorsWithConstraints() {
    for (TestCase<Tuple> each : generateTestSuite(
        singletonList(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")),
        singletonList(new Constraint(){
          @Override
          public boolean test(Tuple tuple) {
            return true;
          }

          @Override
          public List<String> involvedKeys() {
            return singletonList("fsm1");
          }
        })
    )) {
      System.out.println(each.get());
    }
  }
  @Test
  public void whenEncodeParameterSpace$thenFactorSpaceGenerated() {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(singleton(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")))
        .build();
    Function<ParameterSpace, FactorSpace> encoder = Config.Builder.forTuple(new Requirement.Builder().withNegativeTestGeneration(false).build()).build().encoder();
    FactorSpace factorSpace = encoder.apply(parameterSpace);
    factorSpace.getFactors().forEach(System.out::println);
    factorSpace.getConstraints().stream().map(TestPredicate::asString).forEach(System.out::println);
  }

  @Test
  public void whenPreprocess2$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpace parameterSpace = preprocess(
        singletonList(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")),
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
        parameterName -> {
          System.out.println(parameterName);
          System.out.println("  " + parameterSpace.getParameter(parameterName));
          parameterSpace.getParameter(parameterName).toFactorSpace().getFactors().forEach(
              factor -> System.out.println("    " + factor)
          );
          parameterSpace.getParameter(parameterName).toFactorSpace().getConstraints().forEach(
              constraint -> System.out.println("    " + constraint)
          );
        }
    );
    parameterSpace.getConstraints().forEach(
        System.out::println
    );
  }

}
