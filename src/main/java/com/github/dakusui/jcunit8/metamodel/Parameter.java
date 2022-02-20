package com.github.dakusui.jcunit8.metamodel;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit.regex.RegexComposer;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.regex.RegexDecomposer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

/**
 * A class that models a test input parameter or parameters.
 *
 * @param <T> Type of values held by this class.
 */
public interface Parameter<T> {
  /**
   * Returns a name of this parameter.
   *
   * @return A name of this parameter.
   */
  String getName();

  /**
   * Encodes this parameter into factors and constraints.
   *
   * @return Encoded factors and constraints.
   */
  FactorSpace toFactorSpace();

  /**
   * Compose a value of this parameter from a tuple.
   * @param tuple an internal representation of a value of this parameter.
   * @return A value of this parameter.
   */
  T composeValue(Tuple tuple);

  /**
   * Decomposes a value into a key-value pair set.
   * @param value
   * @return
   */
  Optional<Tuple> decomposeValue(T value);

  /**
   * Returns a list of "known values" of this parameter.
   * In case `Simple` parameter model, the values given on its construction are returned.
   * For non-simple parameters, those are the ones the user directly specifies.
   * To implement "seeding" feature such values are important, because users are interested in the user level representation,
   * but not in the internal representation.
   *
   * @return A list of known values of this parameter.
   */
  List<T> getKnownValues();

  interface Factory<T> {
    <F extends Factory<T>> F addActualValue(T actualValue);

    <F extends Factory<T>> F addActualValues(List<T> actualValues);

    Parameter<T> create(String name);

    abstract class Base<T> implements Factory<T> {

      protected final List<T> knownValues = new LinkedList<>();

      @SuppressWarnings("unchecked")
      @Override
      public <F extends Factory<T>> F addActualValue(T actualValue) {
        knownValues.add(actualValue);
        return (F) this;
      }

      @SuppressWarnings("unchecked")
      @Override
      public <F extends Factory<T>> F addActualValues(List<T> actualValues) {
        actualValues.forEach(this::addActualValue);
        return (F) this;
      }
    }
  }

  abstract class Base<T> implements Parameter<T> {
    protected final String  name;
    private final   List<T> knownValues;

    protected Base(String name, List<T> knownValues) {
      this.name = requireNonNull(name);
      this.knownValues = unmodifiableList(requireNonNull(knownValues));
    }

    protected static <V> Optional<Tuple> _decomposeValue(V value, Stream<Tuple> tuples, Function<Tuple, V> valueComposer, Predicate<Tuple> constraints) {
      return tuples.filter((Tuple tuple) -> value.equals(valueComposer.apply(tuple)))
          .filter(constraints)
          .findFirst();
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public FactorSpace toFactorSpace() {
      return FactorSpace.create(decompose(), generateConstraints());
    }

    @Override
    public List<T> getKnownValues() {
      return this.knownValues;
    }

    protected abstract List<Factor> decompose();

    protected abstract List<Constraint> generateConstraints();
  }
}



