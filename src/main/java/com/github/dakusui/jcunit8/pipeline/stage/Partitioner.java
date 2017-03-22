package com.github.dakusui.jcunit8.pipeline.stage;

import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface Partitioner extends Function<List<FactorSpace>, List<FactorSpace>> {
  class Base implements Partitioner {
    @Override
    public List<FactorSpace> apply(List<FactorSpace> factorSpaces) {
      List<Constraint> constraints = factorSpaces.stream().flatMap(factorSpace -> factorSpace.getConstraints().stream()).collect(toList());
      List<List<Constraint>> groupedConstraints = new LinkedList<>();
      while (!constraints.isEmpty()) {
        groupedConstraints.add(findConnectedConstraints(constraints.remove(0), constraints));
      }

      List<FactorSpace> ret = new LinkedList<>();
      List<Factor> factors = factorSpaces.stream().flatMap(factorSpace -> factorSpace.getFactors().stream()).collect(toList());
      for (List<Constraint> eachConstraintGroup : groupedConstraints) {
        List<String> involvedKeys = Utils.unique(eachConstraintGroup.stream().flatMap(constraint -> constraint.involvedKeys().stream()).collect(toList()));
        List<Factor> involvedFactors = factors.stream().filter(factor -> involvedKeys.contains(factor.getName())).collect(toList());
        ret.add(FactorSpace.Internal.create(
            involvedFactors,
            eachConstraintGroup
        ));
        factors.removeAll(involvedFactors);
      }

      ret.add(FactorSpace.Internal.create(factors, Collections.emptyList()));
      return ret;
    }

    private List<Constraint> findConnectedConstraints(Constraint constraint, List<Constraint> in) {
      assert !in.contains(constraint);
      List<Constraint> work = findDirectlyConnectedConstraints(constraint, in);
      for (Constraint each : new ArrayList<>(work)) {
        work.addAll(findConnectedConstraints(each, in));
      }
      return work;
    }

    private List<Constraint> findDirectlyConnectedConstraints(Constraint constraint, List<Constraint> in) {
      List<Constraint> ret = new LinkedList<>();
      for (Constraint each : new ArrayList<>(in)) {
        if (!Collections.disjoint(constraint.involvedKeys(), each.involvedKeys())) {
          ret.add(each);
          in.remove(each);
        }
      }
      return ret;
    }
  }
}
