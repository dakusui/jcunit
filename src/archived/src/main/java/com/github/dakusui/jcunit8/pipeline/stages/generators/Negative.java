package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.combinatoradix.Cartesianator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

public class Negative extends Generator.Base {
  private final List<Tuple> regularTestCases;
  private final List<Tuple> seeds;

  public Negative(List<Tuple> regularTestCases, List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(factorSpace, requirement);
    this.regularTestCases = regularTestCases;
    this.seeds = seeds;
  }

  @Override
  public List<Tuple> generateCore() {
    return generateNegativeTests(this.regularTestCases, factorSpace, seeds);
  }

  private static List<Tuple> generateNegativeTests(List<Tuple> tuples, FactorSpace factorSpace, List<Tuple> seeds) {
    return new LinkedList<Tuple>() {{
      //noinspection SimplifiableConditionalExpression
      factorSpace.getConstraints(
      ).stream(
      ).filter(
          (Constraint each) -> seeds.stream(
          ).filter(
              ////
              //   If there exists any seed which violates one and only one constraint,
              // a (negative) test case to violate the constraint doesn't need to be generated.
              //   Such a constraint is filtered out here.
              (Tuple seed) -> factorSpace.getConstraints(
              ).stream(
              ).allMatch(
                  (Constraint constraint) ->
                      each == constraint ?
                          !constraint.test(seed) : // Violates each constraint
                          constraint.test(seed)    // But not violate any others
              )
          ).collect(toList()).size() != 1
      ).forEach(
          (Constraint each) ->
              createNegativeTestForConstraint(
                  each,
                  exclude(
                      each,
                      factorSpace.getConstraints()
                  ),
                  composeFactorMap(factorSpace),
                  tuples
              ).ifPresent(
                  this::add
              )
      );
    }};
  }

  private static List<Constraint> exclude(Constraint target, List<Constraint> all) {
    return new LinkedList<Constraint>(all) {{
      remove(target);
    }};
  }

  private static Map<String, List<Object>> composeFactorMap(FactorSpace factorSpace) {
    return new LinkedHashMap<String, List<Object>>() {{
      factorSpace.getFactors().forEach((Factor each) -> put(each.getName(), each.getLevels()));
    }};
  }

  private static Optional<Tuple> createNegativeTestForConstraint(Constraint target, List<Constraint> rest, Map<String, List<Object>> parameters, List<Tuple> tuples) {
    long leastCollateralConstraints = rest.size();
    Optional<Tuple> ret = Optional.empty();
    OUTER:
    for (Tuple base : tuples) {
      for (List<Object> each : createCartesianator(target, parameters)) {
        Tuple modified = modifyTupleWithValues(base, composeValues(target.involvedKeys(), each));
        if (target.test(modified))
          continue;
        long numCollaterals = rest.stream().filter(constraint -> !constraint.test(modified)).count();
        if (numCollaterals != 0) {
          if (numCollaterals < leastCollateralConstraints) {
            leastCollateralConstraints = numCollaterals;
            ret = Optional.of(modified);
          }
          continue;
        }
        ret = Optional.of(modified);
        break OUTER;
      }
    }
    return ret;
  }

  private static Tuple composeValues(List<String> involvedKeys, List<Object> values) {
    return new Tuple.Impl() {{
      AtomicInteger i = new AtomicInteger(0);
      involvedKeys.forEach((String eachKey) -> put(eachKey, values.get(i.getAndIncrement())));
    }};
  }

  private static Tuple modifyTupleWithValues(Tuple in, Tuple values) {
    return new Tuple.Builder()
        .putAll(in)
        .putAll(new HashMap<String, Object>() {{
          values.keySet().forEach(eachKey -> put(eachKey, values.get(eachKey)));
        }})
        .build();
  }

  private static Cartesianator<Object> createCartesianator(final Constraint target, final Map<String, List<Object>> parameters) {
    return new Cartesianator<Object>(target.involvedKeys().stream().map(parameters::get).collect(toList())) {
    };
  }

}
