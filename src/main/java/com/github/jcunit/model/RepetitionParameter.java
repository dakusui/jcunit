package com.github.jcunit.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.Parameter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class RepetitionParameter<T> extends Parameter.Base<List<T>> implements Parameter<List<T>> {

  /**
   * An interface that defines a factor to generate a sequence of `T`.
   *
   * @param <T>
   */
  public interface RepetitionGenerator<T> extends Predicate<Integer> {
    Supplier<T> generator();
  }

  private final RepetitionGenerator<T> emptyRepetitionGenerator = new RepetitionGenerator<T>() {
    @Override
    public boolean test(Integer integer) {
      return Objects.equals(integer, 0);
    }

    @Override
    public Supplier<T> generator() {
      return () -> {
        throw new NoSuchElementException();
      };
    }
  };


  private final List<RepetitionGenerator<T>> generators;
  private final List<T> symbols;
  private final List<Integer> numRepetitions;
  private final Function<List<T>, RepetitionGenerator<T>> primeSequenceGeneratorFunction;

  /**
   * You can specify known values of this object through `knownValues` parameter.
   * Thia can be used to model "seed" values or "negative" values.
   *
   * @param generators  A list of `SequenceGenerator` object.
   * @param name        A name of a factor created by this `Parameter`.
   * @param knownValues Known values of this parameter.
   */
  public RepetitionParameter(String name,
                             List<List<T>> knownValues,
                             List<T> symbols,
                             List<RepetitionGenerator<T>> generators,
                             List<Integer> numRepetitions) {
    super(name, knownValues);
    this.symbols = symbols;
    this.generators = generators;
    this.numRepetitions = numRepetitions;
    this.primeSequenceGeneratorFunction = RepetitionParameter::sequenceGeneratorFor;
  }

  @Override
  protected List<Factor> decompose() {
    return Stream.of(Factor.create(getName() + "#symbols", symbols.toArray(new Object[0])),
                     Factor.create(factorNameForNumRepetitions(this), numRepetitions.toArray(new Object[0])),
                     Factor.create(factorNameForGenerator(this), generators.toArray(new Object[0])))
                 .collect(toList());
  }

  @Override
  protected List<Constraint> generateConstraints() {
    return singletonList(constraintGeneratorIsApplicableToNumRepetitions(this));
  }


  @Override
  public List<T> composeValue(Tuple tuple) {
    Integer numRepetitions = (Integer) tuple.get(factorNameForNumRepetitions(this));
    Supplier<T> generator = generators.get(numRepetitions).generator();
    List<T> ret = new ArrayList<>(numRepetitions);
    for (int i = 0; i < numRepetitions; i++) {
      ret.add(generator.get());
    }
    return ret;
  }

  @Override
  public Optional<Tuple> decomposeValue(List<T> sequence) {
    if (!new HashSet<>(this.symbols).containsAll(sequence))
      return Optional.empty();
    if (!this.numRepetitions.contains(sequence.size()))
      return Optional.empty();
    Tuple.Builder b = new Tuple.Builder();
    for (RepetitionGenerator<T> generator : generators) {
      if (!generator.test(sequence.size()))
        continue;
      List<T> workSequence = new ArrayList<>(sequence.size());
      for (int i = 0; i < sequence.size(); i++) {
        workSequence.add(generator.generator().get());
      }
      if (workSequence.equals(sequence)) {
        b.put(factorNameForNumRepetitions(this), sequence.size());
        b.put(factorNameForGenerator(this), primeSequenceGeneratorFunction.apply(sequence));
        return Optional.of(b.build());
      }
    }
    return Optional.empty();
  }

  private static <T> String factorNameForGenerator(Parameter<T> parameter) {
    return parameter.getName() + "#generators";
  }

  private static <T> String factorNameForNumRepetitions(Parameter<T> parameter) {
    return parameter.getName() + "#numRepetitions";
  }

  private static <T> Constraint constraintGeneratorIsApplicableToNumRepetitions(final Parameter<T> paramter) {
    return new Constraint() {
      @Override
      public String getName() {
        return "generator#test(numRepetitions)->true";
      }

      @Override
      public boolean test(Tuple tuple) {
        Integer numRepetitions = (Integer) tuple.get(factorNameForNumRepetitions(paramter));
        @SuppressWarnings("unchecked") RepetitionGenerator<T> generator = (RepetitionGenerator<T>) tuple.get(factorNameForGenerator(paramter));
        return generator.test(numRepetitions);
      }

      @Override
      public List<String> involvedKeys() {
        return asList(factorNameForGenerator(paramter),
                      factorNameForNumRepetitions(paramter));
      }

      @Override
      public boolean isExplicit() {
        return false;
      }
    };
  }

  private static <T> RepetitionGenerator<T> sequenceGeneratorFor(List<T> value) {
    return new RepetitionGenerator<T>() {
      @Override
      public boolean test(Integer integer) {
        return Objects.equals(value.size(), integer);
      }

      @Override
      public Supplier<T> generator() {
        List<T> data = new ArrayList<>(value);
        AtomicInteger i = new AtomicInteger(0);
        return () -> data.get(i.getAndIncrement());
      }
    };
  }
}
