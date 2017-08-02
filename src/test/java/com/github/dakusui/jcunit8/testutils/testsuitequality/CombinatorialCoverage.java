package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableCartesianator;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.subtuplesOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum CombinatorialCoverage {
  ;

  public static List<Tuple> coveredTuples(int strength, Collection<Tuple> in) {
    return Utils.unique(
        in.stream().flatMap((Tuple i) -> subtuplesOf(i, strength).stream()).collect(toList())
    );
  }

  public static <T> List<T> subtract(Collection<T> a, Collection<T> b) {
    return a.stream(
    ).filter(
        eachInA -> !b.contains(eachInA)
    ).collect(
        toList()
    );
  }


  public static Parameter<Object> createParameter(String name, Object... levels) {
    return Parameter.Simple.Factory.of(asList(levels)).create(name);
  }

  public static List<Tuple> allPossibleTuples(int strength, Parameter... parameters) {
    return StreamSupport.stream(
        new Combinator<>(
            asList(parameters), strength
        ).spliterator(), false
    ).flatMap(
        chosenParameters -> new StreamableCartesianator<>(
            convertParameters(chosenParameters)
        ).stream(
        ).map(
            chosenValues -> new Tuple.Builder() {{
              for (int i = 0; i < chosenParameters.size(); i++)
                put(chosenParameters.get(i).getName(), chosenValues.get(i));
            }}.build()
        )
    ).distinct(
    ).collect(
        toList()
    );
  }

  private static List<List<Object>> convertParameters(List<Parameter> parameters) {
    return parameters.stream(
    ).map(
        (Function<Parameter, List<Object>>) Parameter::getKnownValues
    ).collect(toList());
  }

  public static class TestSuiteBuilder {
    private final List<Tuple>      seeds       = new LinkedList<>();
    private final List<Parameter>  parameters  = new LinkedList<>();
    private final List<Constraint> constraints = new LinkedList<>();

    private int strength;

    public TestSuiteBuilder() {
      this.setStrength(2);
    }

    public TestSuiteBuilder setStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public TestSuiteBuilder addSeed(Tuple tuple) {
      this.seeds.add(tuple);
      return this;
    }

    public TestSuiteBuilder addConstraint(Predicate<Tuple> constraint, String... args) {
      this.constraints.add(Constraint.create(constraint, args));
      return this;
    }

    public TestSuiteBuilder addParameter(String name, Object... levels) {
      this.parameters.add(createParameter(name, levels));
      return this;
    }

    public TestSuite buildTestSuite() {
      return new Pipeline.Standard().execute(
          new Config.Builder(
              new Requirement.Builder(
              ) {{
                seeds.forEach(TestSuiteBuilder.this::addSeed);
              }}.withStrength(
                  this.strength
              ).build()
          ).build(),
          new ParameterSpace.Builder(
          ).addAllConstraints(
              constraints
          ).addAllParameters(
              parameters
          ).build()
      );
    }
  }

  public static void main(String... args) {
    TestSuite testSuite = new TestSuiteBuilder(
    ).setStrength(
        2
    ).addParameter(
        "a", 0, 1
    ).addParameter(
        "b", 0, 1
    ).addParameter(
        "c", 0, 1
    ).addConstraint(
        tuple -> tuple.get("a").equals(tuple.get("b")),
        "a", "b"
    ).buildTestSuite(
    );

    System.out.println("testSuite");
    testSuite.forEach(
        System.out::println
    );

    List<Tuple> allPossibleTuples = allPossibleTuples(
        2,
        createParameter("a", 0, 1),
        createParameter("b", 0, 1),
        createParameter("c", 0, 1)
    );

    System.out.println("allPossibleTuples");
    allPossibleTuples.forEach(
        System.out::println
    );

    List<Tuple> coveredTuples = coveredTuples(
        2,
        testSuite.stream().map(
            TestCase::get
        ).collect(
            toList()
        )
    );

    System.out.println("coveredTuples");
    coveredTuples.forEach(
        System.out::println
    );
    System.out.println(
        coveredTuples.containsAll(allPossibleTuples)
    );

    System.out.println("notCovered");
    System.out.println(
        subtract(allPossibleTuples, coveredTuples)
    );
  }

}
