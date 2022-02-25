package com.github.dakusui.jcunitx.pipeline.stages;

import com.github.dakusui.jcunitx.utils.Utils;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.pipeline.Requirement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * The size of any element in the list this stage produces must not be less than
 * the strength returned by {@link Requirement#strength()}.
 */
public interface Partitioner extends Function<FactorSpace, List<FactorSpace>> {
  class Standard implements Partitioner {
    private final Requirement requirement;

    public Standard(Requirement requirement) {
      this.requirement = requireNonNull(requirement);
    }

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
      List<Factor> factors = new ArrayList<>(factorSpace.getFactors());
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
      return pack(requirement.strength(), ret);
    }

    private static List<FactorSpace> pack(int requiredStrength, List<FactorSpace> factorSpaces) {
      if (factorSpaces.size() <= 1)
        return factorSpaces;
      List<FactorSpace> work = new LinkedList<FactorSpace>() {{
        addAll(factorSpaces.stream().sorted(Comparator.comparingInt(o -> o.getFactors().size())).collect(Collectors.toList()));
      }};
      FactorSpace cur = work.get(0);
      if (cur.getFactors().size() >= requiredStrength)
        return work;
      List<FactorSpace> ret = new LinkedList<>();
      for (FactorSpace each : work.subList(1, work.size())) {
        if (cur.getFactors().size() < requiredStrength)
          cur = add(cur, each);
        else {
          ret.add(requireNonNull(cur));
          cur = each;
        }
      }
      ret.add(requireNonNull(cur));
      return ret;
    }

    private static FactorSpace add(FactorSpace a, FactorSpace b) {
      return FactorSpace.create(
          new ArrayList<Factor>(a.getFactors().size() + b.getFactors().size()) {{
            addAll(a.getFactors());
            addAll(b.getFactors());
          }},
          new ArrayList<Constraint>(a.getConstraints().size() + b.getFactors().size()) {{
            addAll(a.getConstraints());
            addAll(b.getConstraints());
          }}
      );
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
}
