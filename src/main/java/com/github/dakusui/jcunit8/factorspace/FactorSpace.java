package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;

public interface FactorSpace {
  static FactorSpace create(List<? extends Factor> factors, List<Constraint> constraints) {
    List<Constraint> work = new ArrayList<>(constraints);
    return new FactorSpace() {
      Map<String, Factor> factorMap = new LinkedHashMap<String, Factor>() {{
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

  default Stream<Tuple> stream() {
    return new StreamableTupleCartesianator(getFactors()).stream();
  }

  default List<String> getFactorNames() {
    return FactorUtils.toFactorNames(this.getFactors());
  }
}
