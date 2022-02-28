package com.github.dakusui.jcunitx.factorspace;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.core.StreamableRowCartesianator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public interface FactorSpace {
  static FactorSpace create(List<? extends Factor> factors, List<Constraint> constraints) {
    List<Constraint> work = new ArrayList<>(constraints);
    return new FactorSpace() {
      final Map<String, Factor> factorMap = new LinkedHashMap<String, Factor>() {{
        factors.forEach(each -> put(each.getName(), each));
      }};

      @SuppressWarnings("unchecked")
      @Override
      public List<Factor> getFactors() {
        return (List<Factor>) factors;
      }

      @Override
      public Factor getFactor(String name) {
        return factorMap.get(name);
      }

      @Override
      public List<Constraint> getConstraints() {
        return work;
      }

      @Override
      public String toString() {
        return format("factors:%s,constraints:%s", factors, constraints);
      }
    };
  }

  List<Constraint> getConstraints();

  List<Factor> getFactors();

  Factor getFactor(String name);

  default Stream<AArray> streamAllPossibleRows() {
    return new StreamableRowCartesianator(getFactors()).stream();
  }

  default List<String> getFactorNames() {
    return FactorUtils.toFactorNames(this.getFactors());
  }

  /**
   * Returns a new factor space that contains all the factor and constraints in this
   * factor space and those passed to this method.
   *
   * @param factors Factors to be added.
   * @param constraints Constraints to be added
   * @return A new factor space.
   */
  default FactorSpace extend(List<Factor> factors, List<Constraint> constraints) {
    return create(
        Stream.concat(this.getFactors().stream(), factors.stream()).collect(toList()),
        Stream.concat(this.getConstraints().stream(), constraints.stream()).collect(toList()));
  }
}
