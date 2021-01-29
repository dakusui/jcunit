package com.github.dakusui.peerj.ext.base;

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

  private static final Function<Integer, String>                    NAME_FORMALIZER = (id) -> String.format("p%d", id);
  private final        Function<Integer, String>                    name;
  private final        Function<Integer, Factor>                    factor;
  private final        Function<Integer, Function<Integer, Object>> value;
  private final        int                                          numFactors;
  private final        Function<String, Optional<String>>           factorNameFormalizer;

  private FactorSpaceNormalizer(
      Function<Integer, String> name,
      Function<Integer, Factor> factor,
      Function<Integer, Function<Integer, Object>> value,
      Function<String, Integer> indexOfFactorName,
      int numFactors) {
    this.name = name;
    this.factor = factor;
    this.value = value;
    this.factorNameFormalizer = factorName ->
        indexOfFactorName.apply(factorName) >= 0
            ? Optional.of(name.apply(indexOfFactorName.apply(factorName)))
            : Optional.empty();
    this.numFactors = numFactors;
  }

  public FactorSpaceNormalizer(FactorSpace factorSpace) {
    this(NAME_FORMALIZER,
        (id) -> factorSpace.getFactors().get(id),
        (i) -> (j) -> factorSpace.getFactors().get(i).getLevels().get(j),
        (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
        factorSpace.getFactors().size());
  }

  public String formalFactorNameOf(int formalFactorId) {
    return this.name.apply(formalFactorId);
  }

  public int numLevelsOfFormalFactor(int formalFactorId) {
    return factorFor(formalFactorId).getLevels().size();
  }

  public Object formalFactorLevelOf(int formalFactorId, int formalFactorLevelId) {
    ////
    // If the formalFactorLevelId is not valid for the factor specified by formalFactorId,
    // an exception will be thrown.
    return this.value.apply(formalFactorId).apply(formalFactorLevelId);
  }

  public Optional<String> formalizeFactorName(String factorName) {
    return this.factorNameFormalizer.apply(factorName);
  }

  public int numFormalFactors() {
    return this.numFactors;
  }

  private Factor factorFor(int formalFactorId) {
    return factor.apply(formalFactorId);
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
