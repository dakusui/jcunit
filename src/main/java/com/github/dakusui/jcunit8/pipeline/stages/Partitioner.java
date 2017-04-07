package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface Partitioner extends Function<FactorSpace, List<FactorSpace>> {
  class Standard implements Partitioner {
    @Override
    public List<FactorSpace> apply(FactorSpace factorSpace) {
      List<Constraint> constraints = new LinkedList<>(factorSpace.getConstraints());
      List<List<Constraint>> groupedConstraints = new LinkedList<>();
      while (!constraints.isEmpty()) {
        groupedConstraints.add(new LinkedList<Constraint>() {
          {
            add(constraints.remove(0));
            addAll(findConnectedConstraints(get(0), constraints));
          }
        });
      }

      List<FactorSpace> ret = new LinkedList<>();
      List<Factor> factors = factorSpace.getFactors();
      for (List<Constraint> eachConstraintGroup : groupedConstraints) {
        List<String> involvedKeys = Utils.unique(eachConstraintGroup.stream().flatMap(constraint -> constraint.involvedKeys().stream()).collect(toList()));
        List<Factor> involvedFactors = factors.stream().filter(factor -> involvedKeys.contains(factor.getName())).collect(toList());
        if (involvedFactors.isEmpty())
          continue;
        ret.add(FactorSpace.create(
            involvedFactors,
            eachConstraintGroup
        ));
        factors.removeAll(involvedFactors);
      }

      ret.add(FactorSpace.create(
          factors,
          factorSpace.getConstraints().stream()
              .filter(constraint -> !Collections.disjoint(
                  constraint.involvedKeys(),
                  factors.stream()
                      .map(Factor::getName)
                      .collect(toList())
              ))
              .collect(toList())
      ));
      return ret;
    }

    private static List<Constraint> findConnectedConstraints(Constraint constraint, List<Constraint> in) {
      assert !in.contains(constraint);
      List<Constraint> work = findDirectlyConnectedConstraints(constraint, in);
      for (Constraint each : new ArrayList<>(work)) {
        work.addAll(findConnectedConstraints(each, in));
      }
      return work;
    }

    private static List<Constraint> findDirectlyConnectedConstraints(Constraint constraint, List<Constraint> in) {
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

  class ConnectedConstraintFinder {
    private final List<Constraint> allConstraints;

    public ConnectedConstraintFinder(List<Constraint> constraints) {
      this.allConstraints = constraints;
    }

    private List<Constraint> find(Constraint constraint) {
      return Standard.findConnectedConstraints(constraint, new ArrayList<Constraint>(this.allConstraints) {{ remove(constraint);}});
    }

    public List<Constraint> findAll(List<Constraint> constraints) {
      LinkedHashSet<Constraint> work = new LinkedHashSet<>();
      for (Constraint each : constraints) {
        work.addAll(find(each));
        work.add(each);
      }
      return new ArrayList<>(work);
    }
  }
}
