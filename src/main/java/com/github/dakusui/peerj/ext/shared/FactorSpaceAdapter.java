package com.github.dakusui.peerj.ext.shared;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.function.Function;

public class FactorSpaceAdapter {
  static final Function<Integer, String> NAME_RESOLVER =
      (id) -> String.format("p%d", id);
  public final Function<Integer, String> name;
  public final Function<Integer, String>                    type;
  public final Function<Integer, Factor>                    factor;
  public final Function<Integer, Function<Integer, Object>> value;
  public final int                      numParameters;
  public final Function<String, String> factorNameToParameterName;

  private FactorSpaceAdapter(
      Function<Integer, String> name,
      Function<Integer, String> type,
      Function<Integer, Factor> factor,
      Function<Integer, Function<Integer, Object>> value,
      Function<String, Integer> indexOfFactorName,
      int numParameters) {
    this.name = name;
    this.type = type;
    this.factor = factor;
    this.value = value;
    this.factorNameToParameterName = factorName ->
        indexOfFactorName.apply(factorName) >= 0
            ? name.apply(indexOfFactorName.apply(factorName))
            : factorName;
    this.numParameters = numParameters;
  }

  public FactorSpaceAdapter(FactorSpace factorSpace) {
    this(NAME_RESOLVER,
        (id) -> "0",
        (id) -> factorSpace.getFactors().get(id),
        (ii) -> (j) -> factorSpace.getFactors().get(ii).getLevels().get(j),
        (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
        factorSpace.getFactors().size());
  }
}
