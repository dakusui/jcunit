package com.github.dakusui.peerj.ext.shared;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.Optional;
import java.util.function.Function;


public class FactorSpaceTranslator {
  private static final Function<Integer, String>                    NAME_FORMALIZER = (id) -> String.format("p%d", id);
  private final        Function<Integer, String>                    name;
  private final        Function<Integer, Factor>                    factor;
  private final        Function<Integer, Function<Integer, Object>> value;
  private final        int                                          numFactors;
  private final        Function<String, Optional<String>>           factorNameFormalizer;

  private FactorSpaceTranslator(
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

  public FactorSpaceTranslator(FactorSpace factorSpace) {
    this(NAME_FORMALIZER,
        (id) -> factorSpace.getFactors().get(id),
        (ii) -> (j) -> factorSpace.getFactors().get(ii).getLevels().get(j),
        (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
        factorSpace.getFactors().size());
  }

  public String formalFactorNameOf(int formalFactorId) {
    return this.name.apply(formalFactorId);
  }

  public Factor factorFor(int formalFactorId) {
    return factor.apply(formalFactorId);
  }

  public Object formalFactorLevelOf(int formalFactorId, int formalFactorLevelId) {
    ////
    // If the formalFactorLevelId is not valid for the factor specified by formalFactorId,
    // an exception will be thrown.
    this.value.apply(formalFactorId).apply(formalFactorLevelId);
    return formalFactorLevelId;
  }

  public Optional<String> formalizeFactorName(String factorName) {
    return this.factorNameFormalizer.apply(factorName);
  }

  public int numFactors() {
    return this.numFactors;
  }
}
