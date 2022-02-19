package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit.regex.RegexComposer;
import com.github.dakusui.jcunit8.core.Utils;
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
import static java.util.stream.Collectors.toList;

/**
 * A user level factor.
 *
 * @param <T> Type of values held by this class.
 */
public interface Parameter<T> {
  String getName();

  FactorSpace toFactorSpace();

  T composeValue(Tuple tuple);

  Optional<Tuple> decomposeValue(T value);

  List<T> getKnownValues();

  abstract class Base<T> implements Parameter<T> {
    protected final String  name;
    private final   List<T> knownValues;

    protected Base(String name, List<T> knownValues) {
      this.name = requireNonNull(name);
      this.knownValues = unmodifiableList(requireNonNull(knownValues));
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

    static <V> Optional<Tuple> _decomposeValue(V value, Stream<Tuple> tuples, Function<Tuple, V> valueComposer, Predicate<Tuple> constraints) {
      return tuples.filter(
          (Tuple tuple) -> value.equals(valueComposer.apply(tuple))
      ).filter(
          constraints
      ).findFirst(
      );
    }
  }

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

  interface Simple<T> extends Parameter<T> {
    class Impl<T> extends Base<T> implements Simple<T> {
      final Factor factor;

      public Impl(String name, List<T> allLevels) {
        super(name, allLevels);
        this.factor = Factor.create(name, allLevels.toArray());
      }

      @Override
      protected List<Factor> decompose() {
        return singletonList(factor);
      }

      @Override
      protected List<Constraint> generateConstraints() {
        return emptyList();
      }

      @SuppressWarnings("unchecked")
      @Override
      public T composeValue(Tuple tuple) {
        return (T) tuple.get(getName());
      }

      @Override
      public Optional<Tuple> decomposeValue(T value) {
        return Optional.of(Tuple.builder().put(name, value).build());
      }

      @Override
      public String toString() {
        return String.format("Simple:%s:%s", factor.getName(), factor.getLevels());
      }
    }

    class Factory<T> extends Parameter.Factory.Base<T> {

      private Factory() {
      }

      public static <U> Factory<U> of(List<U> actualValues) {
        return new Factory<U>().addActualValues(actualValues);
      }

      @Override
      public Parameter<T> create(String name) {
        return new Impl<>(name, this.knownValues);
      }
    }
  }

  interface Regex extends Parameter<List<String>> {
    class Impl extends Parameter.Base<List<String>> implements Regex {

      private final FactorSpace         factorSpace;
      private final RegexComposer       regexComposer;

      public Impl(String name, String regex, List<List<String>> knownValues) {
        super(name, knownValues);
        Expr expr = new Parser().parse(regex);
        RegexDecomposer decomposer = new RegexDecomposer(name, expr);
        this.regexComposer = new RegexComposer(name, expr);
        this.factorSpace = decomposer.decompose();
      }

      public Optional<Tuple> decomposeValue(List<String> value) {
        return _decomposeValue(
            value,
            this.factorSpace.stream(),
            this.regexComposer::compose,
            Utils.conjunct(this.factorSpace.getConstraints())
        );
      }

      @Override
      public List<String> composeValue(Tuple tuple) {
        return new ArrayList<>(composeStringValueFrom(tuple));
      }

      @Override
      protected List<Factor> decompose() {
        return factorSpace.getFactors();
      }

      @Override
      protected List<Constraint> generateConstraints() {
        return factorSpace.getConstraints();
      }

      private List<String> composeStringValueFrom(Tuple tuple) {
        return regexComposer.compose(tuple);
      }
    }

    class Factory<T> extends Parameter.Factory.Base<List<String>> {
      private final String              regex;

      @Override
      public Regex create(String name) {
        return create(name, regex, knownValues);
      }

      public static Factory<String> of(String regex) {
        return new Factory<>(regex);
      }

      private Factory(String regex) {
        this.regex = requireNonNull(regex);
      }

      private static <U> Regex create(String name, String regex, List<List<String>> knownValues) {
        return new Impl(name, regex, knownValues);
      }
    }
  }
}



