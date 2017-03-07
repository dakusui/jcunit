package com.github.dakusui.jcunit8.model.parameterspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.model.factorspace.Constraint;
import com.github.dakusui.jcunit8.model.factorspace.Factor;
import com.github.dakusui.jcunit8.model.factorspace.FactorSpace;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public interface Parameter<T> {
  String getName();

  List<Factor> getFactors();

  List<Constraint.ForTuple> getConstraints();

  FactorSpace.Characteristics getCharacteristics();

  Factor toFactor(List<Tuple> tuples);

  abstract class Base<T> implements Parameter<T> {
    private final String name;

    Base(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public Factor toFactor(List<Tuple> tuples) {
      return Utils.createFactor(
          this.getName(),
          tuples.stream()
              .map(this::composeValueFrom)
              .collect(Collectors.toList())
              .toArray(new Object[tuples.size()]));
    }

    @Override
    public FactorSpace.Characteristics getCharacteristics() {
      return new FactorSpace.Characteristics() {
        @Override
        public int numberOfFactors() {
          return getFactors().size();
        }

        @Override
        public double averageNumberOfLevels() {
          OptionalDouble value;
          return (value = getFactors().stream()
              .map(Factor::getLevels)
              .mapToInt(List::size).average()
          ).isPresent() ?
              value.getAsDouble() :
              Double.NaN;
        }
      };
    }


    abstract T composeValueFrom(Tuple tuple);
  }

  interface Simple<T> extends Parameter<T> {
    abstract class Impl<T> extends Base<T> implements Simple<T> {
      Impl(String name) {
        super(name);
      }
    }

    static <T> Simple<T> create(String name, Object... args) {
      Factor factor = Utils.createFactor(name, args);

      return new Impl<T>(name) {
        @Override
        T composeValueFrom(Tuple tuple) {
          //noinspection unchecked
          return (T) tuple.get(name);
        }

        @Override
        public List<Factor> getFactors() {
          return singletonList(factor);
        }

        @Override
        public List<Constraint.ForTuple> getConstraints() {
          return emptyList();
        }
      };
    }

  }

  interface Fsm extends Parameter {
  }

  interface Regex<T> extends Parameter<List<T>> {
  }
}
