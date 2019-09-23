package com.github.dakusui.jcunit8.testutils.testsuitequality;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.crest.matcherbuilders.primitives.AsBoolean;
import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableCartesianator;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestSuite;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.jcunit.core.tuples.TupleUtils.subtuplesOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum CoveringArrayGenerationUtils {
  ;

  public static FactorSpace mergeFactorSpaces(FactorSpace lhsFactorSpace, FactorSpace rhsFactorSpace) {
    return FactorSpace.create(
        Stream.concat(
            lhsFactorSpace.getFactors().stream(), rhsFactorSpace.getFactors().stream()
        ).collect(toList()),
        Stream.concat(
            lhsFactorSpace.getConstraints().stream(), rhsFactorSpace.getConstraints().stream()
        ).collect(toList())
    );
  }

  public static FactorSpace createFactorSpace(String prefix, int numLevels, int numFactors) {
    return toFactorSpace(
        parameterSpace(
            IntStream.range(0, numFactors).mapToObj(
                i -> String.format("%s-%02d", prefix, i)
            ).map(
                name -> p(name, IntStream.range(0, numLevels).boxed().collect(toList()).toArray())
            ).collect(toList()),
            Collections.emptyList()
        )
    );
  }

  public static ParameterSpace parameterSpace(List<Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  public static FactorSpace factorSpace(List<Parameter> parameters, List<Constraint> constraints) {
    return toFactorSpace(parameterSpace(parameters, constraints));
  }

  public static FactorSpace toFactorSpace(ParameterSpace parameterSpace) {
    return FactorSpace.create(
        parameterSpace.getParameterNames().stream(
        ).map(
            parameterSpace::getParameter
        ).map(
            parameter -> Factor.create(parameter.getName(), parameter.getKnownValues().toArray())
        ).collect(
            toList()
        ),
        parameterSpace.getConstraints()
    );
  }

  public static List<Tuple> generateWithIpoGplus(FactorSpace factorSpace, int strength) {
    return new IpoGplus(
        factorSpace,
        new Requirement.Builder().withStrength(strength).build(),
        Collections.emptyList()
    ).generate();
  }

  public static List<Tuple> join(List<Tuple> lhs, List<Tuple> rhs, Function<Requirement, Joiner> joinerFactory, int strength) {
    return joinerFactory.apply(new Requirement.Builder().withStrength(strength).build()).apply(
        SchemafulTupleSet.fromTuples(lhs),
        SchemafulTupleSet.fromTuples(rhs)
    );
  }

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

  /*
   * Returns an example if any.
   */
  public static Optional<Tuple> findAllowedSuperTupleFor(Tuple tuple, ParameterSpace parameterSpace) {
    List<String> allKeysUsedByConstraintsButNotInTuple = parameterSpace.getConstraints().stream(
    ).flatMap(
        constraint -> constraint.involvedKeys().stream()
    ).filter(
        key -> !tuple.containsKey(key)
    ).distinct(
    ).collect(
        toList()
    );
    if (allKeysUsedByConstraintsButNotInTuple.isEmpty())
      return parameterSpace.getConstraints().stream().allMatch(constraint -> constraint.test(tuple)) ?
          Optional.of(tuple) :
          Optional.empty();
    return new StreamableCartesianator<>(
        allKeysUsedByConstraintsButNotInTuple.stream().map(
            key -> parameterSpace.getParameter(key).getKnownValues()
        ).collect(
            toList()
        )
    ).stream(
    ).map(
        values -> {
          Tuple.Builder builder = Tuple.builder();
          for (int i = 0; i < allKeysUsedByConstraintsButNotInTuple.size(); i++) {
            builder.put(allKeysUsedByConstraintsButNotInTuple.get(i), values.get(i));
          }
          tuple.keySet().forEach(
              key -> builder.put(key, tuple.get(key))
          );
          return builder.build();
        }
    ).filter(
        t -> parameterSpace.getConstraints().stream().allMatch(each -> each.test(t))
    ).findFirst(
    );
  }

  public static Parameter<Object> p(String name, Object... levels) {
    return Parameter.Simple.Factory.of(asList(levels)).create(name);
  }

  public static List<Tuple> allPossibleTuplesInFactors(int strength, List<Factor> factors) {
    return allPossibleTuples(
        strength,
        factors.stream().map(
            f -> p(f.getName(), f.getLevels().toArray())
        ).collect(
            toList()
        ));
  }

  public static List<Tuple> allPossibleTuples(int strength, List<Parameter> parameters) {
    return allPossibleTuples(strength, parameters.toArray(new Parameter[0]));
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

  @SuppressWarnings("RedundantCast")
  private static List<List<Object>> convertParameters(List<Parameter> parameters) {
    return parameters.stream()
        .map((Function<Parameter, List<Object>>) Parameter::getKnownValues)
        .collect(toList());
  }

  static TestSuite buildTestSuite(int strength, List<Parameter> parameters, List<Constraint> constraints) {
    return new TestSuiteBuilder(
    ) {{
      parameters.forEach(each -> addParameter(each.getName(), each.getKnownValues().toArray()));
      constraints.forEach(each -> addConstraint(each.getName(), each, each.involvedKeys().toArray(new String[0])));
    }}.setStrength(
        strength
    ).buildTestSuite();
  }

  public static List<Constraint> constraints(Constraint... constraints) {
    return asList(constraints);
  }

  public static Constraint c(Predicate<Tuple> constraint, String... involvedKeys) {
    return Constraint.create(String.format("c%s", Arrays.toString(involvedKeys)), constraint, involvedKeys);
  }

  public static List<Parameter> parameters(Parameter... parameters) {
    return asList(parameters);
  }

  static <I> AsBoolean<? super I> failsIf(boolean condition) {
    return asBoolean(Printable.function(
        "debugMode",
        (Function<I, Boolean>) (t -> !condition)
    )).isTrue();
  }

  public static void assertCoveringArray(List<Tuple> coveringArray, FactorSpace factorSpace, int strength) {
    //    System.out.println("== " + coveringArray.size() + " ==");
    //    coveringArray.forEach(System.out::println);
    System.out.println("Verifying covering array: numFactors=" + factorSpace + ", strength=" + strength);
    StopWatch stopWatch = new StopWatch();
    try {
      assertThat(
          coveringArray,
          asListOf(
              Tuple.class,
              Printable.function(
                  "coveredTuples",
                  (List<Tuple> ca) -> coveredTuples(strength, ca)
              )
          ).containsAll(
              allPossibleTuplesInFactors(
                  strength,
                  factorSpace.getFactors())
          ).$()
      );
    } finally {
      System.out.println(stopWatch.get() + " [msec]");
    }
  }

  public static class TestSuiteBuilder {
    private final List<Tuple>      seeds       = new LinkedList<>();
    private final List<Parameter>  parameters  = new LinkedList<>();
    private final List<Constraint> constraints = new LinkedList<>();

    private int strength;

    TestSuiteBuilder() {
      this.setStrength(2);
    }

    public TestSuiteBuilder setStrength(int strength) {
      this.strength = strength;
      return this;
    }

    TestSuiteBuilder addSeed(Tuple tuple) {
      this.seeds.add(tuple);
      return this;
    }

    TestSuiteBuilder addConstraint(String name, Predicate<Tuple> constraint, String... args) {
      this.constraints.add(Constraint.create(name, constraint, args));
      return this;
    }

    TestSuiteBuilder addParameter(String name, Object... levels) {
      this.parameters.add(p(name, levels));
      return this;
    }

    TestSuite buildTestSuite() {
      return new Pipeline.Standard().execute(
          new Config.Builder(
              new Requirement.Builder(
              ) {{
                seeds.forEach(TestSuiteBuilder.this::addSeed);
              }}.withStrength(
                  this.strength
              ).build()
          ).build(),
          parameterSpace(parameters, constraints),
          null
      );
    }
  }

  public static class StopWatch {
    long last = System.currentTimeMillis();

    public long get() {
      return -last + (last = System.currentTimeMillis());
    }

    public static void main(String... args) throws InterruptedException {
      StopWatch stopWatch = new StopWatch();
      Thread.sleep(100);
      System.out.println(stopWatch.get());
    }
  }
}
