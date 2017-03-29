package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FiniteStateMachine;
import com.github.dakusui.jcunit.regex.Composer;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit8.factorspace.fsm.FsmComposer;
import com.github.dakusui.jcunit8.factorspace.fsm.FsmDecomposer;
import com.github.dakusui.jcunit8.factorspace.fsm.Player;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.factorspace.regex.RegexDecomposer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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

  T composeValueFrom(Tuple tuple);

  List<T> getKnownValues();

  boolean check(T value);

  abstract class Base<T> implements Parameter<T> {
    protected final String       name;
    protected final Predicate<T> check;
    private final   List<T>      knownValues;

    Base(String name, List<T> knownValues, Predicate<T> check) {
      this.name = requireNonNull(name);
      this.check = requireNonNull(check);
      this.knownValues = unmodifiableList(requireNonNull(knownValues));
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public boolean check(T value) {
      return check.test(value);
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

  interface Factory<T> {
    <F extends Factory<T>> F addActualValue(T actualValue);

    <F extends Factory<T>> F addActualValues(List<T> actualValues);

    <F extends Factory<T>> F setCheck(Predicate<T> check);

    Parameter<T> create(String name);

    abstract class Base<T> implements Factory<T> {

      protected final List<T>      knownValues = new LinkedList<>();
      protected       Predicate<T> check       = t -> true;

      @Override
      public <F extends Factory<T>> F addActualValue(T actualValue) {
        knownValues.add(actualValue);
        //noinspection unchecked
        return (F) this;
      }

      @Override
      public <F extends Factory<T>> F addActualValues(List<T> actualValues) {
        actualValues.forEach(this::addActualValue);
        //noinspection unchecked
        return (F) this;
      }

      @Override
      public <F extends Factory<T>> F setCheck(Predicate<T> check) {
        this.check = requireNonNull(check);
        //noinspection unchecked
        return (F) this;
      }
    }
  }

  interface Simple<T> extends Parameter<T> {
    static <T> Constraint createConstraintFrom(Parameter.Simple<T> parameter) {
      return new Constraint() {
        @Override
        public boolean test(Tuple testObject) {
          return parameter.check(parameter.composeValueFrom(testObject));
        }

        @Override
        public List<String> involvedKeys() {
          return singletonList(parameter.getName());
        }
      };
    }

    class Impl<T> extends Base<T> implements Simple<T> {
      final Factor factor;

      public Impl(String name, List<T> allLevels, Predicate<T> validityChecker) {
        super(name, allLevels, validityChecker);
        this.factor = Factor.create(name, allLevels.stream().filter(Impl.this::check).collect(toList()).toArray());
      }

      @Override
      protected List<Factor> decompose() {
        return singletonList(factor);
      }

      @Override
      protected List<Constraint> generateConstraints() {
        return emptyList();
      }

      @Override
      public T composeValueFrom(Tuple tuple) {
        //noinspection unchecked
        return (T) tuple.get(getName());
      }

      @Override
      public boolean check(T value) {
        return check.test(value);
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
        return new Impl<>(name, this.knownValues, this.check);
      }
    }
  }

  interface Regex<T> extends Parameter<List<T>> {
    class Impl<U> extends Parameter.Base<List<U>> implements Regex<U> {

      private final FactorSpace         factorSpace;
      private final Composer            composer;
      private final Function<String, U> func;

      public Impl(String name, String regex, List<List<U>> knownValues, Function<String, U> func, Predicate<List<U>> check) {
        super(name, knownValues, check);
        Expr expr = new Parser().parse(regex);
        RegexDecomposer translator = new RegexDecomposer(name, expr);
        this.func = func;
        this.composer = new Composer(name, expr);
        this.factorSpace = translator.decompose();
      }


      @Override
      public List<U> composeValueFrom(Tuple tuple) {
        return composeStringValueFrom(tuple).stream().map(func).collect(toList());
      }

      @Override
      public boolean check(List<U> value) {
        return this.check.test(value);
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
        return composer.compose(tuple);
      }
    }

    class Factory<T> extends Parameter.Factory.Base<List<T>> {
      private final String              regex;
      private final Function<String, T> func;

      @Override
      public Regex<T> create(String name) {
        return create(name, regex, knownValues, func, check);
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

      private static <U> Regex<U> create(String name, String regex, List<List<U>> knownValues, Function<String, U> func, Predicate<List<U>> check) {
        return new Impl<>(name, regex, knownValues, func, check);
      }
    }
  }

  interface Fsm<SUT> extends Parameter<Scenario<SUT>> {

    class Impl<SUT> extends Parameter.Base<Scenario<SUT>> {
      private final FsmDecomposer<SUT> decomposer;
      private final FsmComposer<SUT>   composer;

      Impl(String name, FiniteStateMachine<SUT> model, List<Scenario<SUT>> knownValues, int scenarioLength, Predicate<Scenario<SUT>> check) {
        super(name, knownValues, check);
        this.decomposer = new FsmDecomposer<>(name, model, scenarioLength);
        this.composer = new FsmComposer<>(name, model, scenarioLength);
      }

      @Override
      public Scenario<SUT> composeValueFrom(Tuple tuple) {
        return composer.composeValueFrom(tuple);
      }

      @Override
      protected List<Factor> decompose() {
        return decomposer.getFactors();
      }

      @Override
      protected List<Constraint> generateConstraints() {
        return decomposer.getConstraints();
      }
    }
  }
}



