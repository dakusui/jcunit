package com.github.jcunit.factorspace;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.regex.RegexDecomposer;
import com.github.jcunit.core.regex.Expr;
import com.github.jcunit.core.regex.RegexParser;
import com.github.jcunit.core.regex.RegexComposer;
import com.github.jcunit.utils.InternalUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.jcunit.factorspace.Factor.VOID;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A user level factor.
 * This interface models a "parameter" that users (testers, programmers, or users of the SUT) recognize.
 *
 * @param <T> Type of values held by this class.
 */
public interface Parameter<T> {
  String getName();

  /**
   * Converts ths parameter into `FactorSpace`.
   *
   * @return a `FactorSpace` to which this `Parameter` is encoded.
   *
   * @see FactorSpace
   */
  FactorSpace toFactorSpace();

  T composeValue(Tuple tuple);

  Optional<Tuple> decomposeValue(T value);

  List<T> getKnownValues();

  abstract class Base<T> implements Parameter<T> {
    protected final String name;
    private final List<T> knownValues;

    /**
     *
     * You can specify known values of this object through `knownValues` parameter.
     * Thia can be used to model "seed" values or "negative" values.
     *
     * @param name A name of a factor created by this `Parameter`.
     * @param knownValues Known values of this parameter.
     */
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
      return tuples.filter((Tuple tuple) -> value.equals(valueComposer.apply(tuple)))
                   .filter(constraints)
                   .findFirst();
    }
  }

  interface Factory<T> {
    <F extends Factory<T>> F addActualValues(List<T> actualValues);

    Parameter<T> create(String name);

    abstract class Base<T> implements Factory<T> {

      protected final List<T> knownValues = new LinkedList<>();

      @SuppressWarnings("unchecked")
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
      private final Factor factor;
      private final List<Constraint> constraints;

      public Impl(String name, List<T> allLevels) {
        this(false, name, allLevels);
      }

      public Impl(boolean withVoid, String name, List<T> allLevels) {
        this(withVoid, name, allLevels, Collections.emptyList());
      }

      public Impl(boolean withVoid, String name, List<T> allLevels, List<Constraint> constraints) {
        super(name, allLevels);
        this.factor = Factor.create(name,
                                    Stream.concat(allLevels.stream(),
                                                  withVoid ? Stream.of(VOID)
                                                           : Stream.empty()).toArray());
        this.constraints = requireNonNull(constraints);
      }

      @Override
      protected List<Factor> decompose() {
        return singletonList(factor);
      }

      @Override
      protected List<Constraint> generateConstraints() {
        return this.constraints;
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

  /**
   * @param <T> Type of element in a sequence generated by a given regular expression.
   */
  interface Regex<T> extends Parameter<List<T>> {
    class Impl<U> extends Parameter.Base<List<U>> implements Regex<U> {

      private final FactorSpace factorSpace;
      private final RegexComposer regexComposer;
      private final Function<String, U> func;

      public Impl(boolean withVoid, String name, String[] regexes, List<List<U>> knownValues, Function<String, U> func) {
        this(withVoid, name, String.join("", regexes), knownValues, func);
      }

      public Impl(boolean withVoid, String name, String regex, List<List<U>> knownValues, Function<String, U> func) {
        super(name, knownValues);
        Expr expr = new RegexParser().parse(regex);
        RegexDecomposer translator = new RegexDecomposer(name, expr);
        this.func = func;
        this.regexComposer = new RegexComposer(name, expr);
        this.factorSpace = translator.decompose();
      }

      public Optional<Tuple> decomposeValue(List<U> value) {
        return _decomposeValue(
            value,
            this.factorSpace.stream(),
            this.regexComposer::compose,
            InternalUtils.conjunct(this.factorSpace.getConstraints())
        );
      }

      @Override
      public List<U> composeValue(Tuple tuple) {
        return composeStringValueFrom(tuple).stream()
                                            .map(func)
                                            .collect(toList());
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

    class Factory<T> extends Parameter.Factory.Base<List<T>> {
      private final String regex;
      private final Function<String, T> func;

      @Override
      public Regex<T> create(String name) {
        return create(name, regex, knownValues, func);
      }

      public static <T> Factory<T> of(String regex, Function<String, T> func) {
        return new Factory<>(regex, func);
      }

      public static Factory<String> of(String regex) {
        return new Factory<>(regex, s -> s);
      }

      private Factory(String regex, Function<String, T> func) {
        this.regex = requireNonNull(regex);
        this.func = requireNonNull(func);
      }

      private static <U> Regex<U> create(String name, String regex, List<List<U>> knownValues, Function<String, U> func) {
        return new Impl<>(false, name, regex, knownValues, func);
      }
    }
  }
}



