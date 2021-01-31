package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.pcond.functions.Functions.stream;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static com.github.dakusui.pcond.functions.Printables.function;
import static com.github.dakusui.pcond.functions.Printables.predicate;
import static java.util.stream.Collectors.toList;


public class FactorSpaceNormalizer {

  private static final Function<Integer, String>                    NAME_NORMALIZER = (id) -> String.format("p%d", id);
  private final        Function<Integer, String>                    name;
  private final        Function<Integer, Factor>                    factor;
  private final        Function<Integer, Function<Integer, Object>> value;
  private final        int                                          numFactors;
  private final        Function<String, Optional<String>>           factorNameNormalizer;

  private FactorSpaceNormalizer(
      Function<Integer, String> name,
      Function<Integer, Factor> factor,
      Function<Integer, Function<Integer, Object>> value,
      Function<String, Integer> indexOfFactorName,
      int numFactors) {
    this.name = name;
    this.factor = factor;
    this.value = value;
    this.factorNameNormalizer = factorName ->
        indexOfFactorName.apply(factorName) >= 0
            ? Optional.of(name.apply(indexOfFactorName.apply(factorName)))
            : Optional.empty();
    this.numFactors = numFactors;
  }

  public FactorSpaceNormalizer(FactorSpace factorSpace) {
    this(NAME_NORMALIZER,
        (id) -> factorSpace.getFactors().get(id),
        (i) -> (j) -> factorSpace.getFactors().get(i).getLevels().get(j),
        (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
        factorSpace.getFactors().size());
  }

  public String normalizedFactorNameOf(int normalizedFactorId) {
    return this.name.apply(normalizedFactorId);
  }

  public int numLevelsOfNormalizedFactor(int normalizedFactorId) {
    return factorFor(normalizedFactorId).getLevels().size();
  }

  public Object normalizedFactorLevelOf(int normalizedFactorId, int normalizedFactorLevelId) {
    ////
    // If the normalizedFactorLevelId is not valid for the factor specified by normalizedFactorId,
    // an exception will be thrown.
    return this.value.apply(normalizedFactorId).apply(normalizedFactorLevelId);
  }

  public Optional<String> normalizedFactorName(String factorName) {
    return this.factorNameNormalizer.apply(factorName);
  }

  public int numNormalizedFactors() {
    return this.numFactors;
  }

  private Factor factorFor(int normalizedFactorId) {
    return factor.apply(normalizedFactorId);
  }


  public static Predicate<Factor> isSupportedFactor() {
    return transform(factorLevels()).check(
        and(
            isEmpty().negate(),
            or(
                transform(stream()).check(allMatch(isInstanceOf(String.class))),
                and(
                    transform(stream()).check(allMatch(isInstanceOf(Integer.class))),
                    isSorted()
                ))));
  }

  private static Function<Factor, List<Object>> factorLevels() {
    return function("getLevels", Factor::getLevels);
  }

  private static Predicate<List<?>> isSorted() {
    return predicate("isSorted", (List<?> v) -> Objects.equals(v, v.stream().sorted().collect(toList())));
  }

  private static Term.Type typeOf(Factor factor) {
    if (Objects.equals(String.class, factor.getLevels().get(0).getClass()))
      return Term.Type.ENUM;
    else if (Objects.equals(Integer.class, factor.getLevels().get(0).getClass()))
      return Term.Type.NUMBER;
    throw new UnsupportedOperationException();
  }
}
