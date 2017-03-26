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
import java.util.stream.Collectors;

public class Negative extends Generator.Base {
  public Negative(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(seeds, factorSpace, requirement);
  }

  @Override
  public List<Tuple> generate() {
    return generateNegativeTests(seeds, factorSpace);
  }

  private static List<Tuple> generateNegativeTests(List<Tuple> tuples, FactorSpace factorSpace) {
    List<Tuple> ret = new LinkedList<>();
    factorSpace.getConstraints().forEach(each -> createNegativeTestForConstraint(each, exclude(each, factorSpace.getConstraints()), composeFactorMap(factorSpace), tuples));
    return ret;
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
    return new Cartesianator<Object>(target.involvedKeys().stream().map(parameters::get).collect(Collectors.toList())) {
    };
  }

}
