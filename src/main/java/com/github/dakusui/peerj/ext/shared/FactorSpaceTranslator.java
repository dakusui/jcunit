package com.github.dakusui.peerj.ext.shared;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.function.Function;

public class FactorSpaceTranslator {
  private static final Function<Integer, String>                    NAME_RESOLVER =
      (id) -> String.format("p%d", id);
  private final        Function<Integer, String>                    name;
  private final        Function<Integer, String>                    type;
  private final        Function<Integer, Factor>                    factor;
  private final        Function<Integer, Function<Integer, Object>> value;
  private final         int                                          numParameters;
  public final         Function<String, String>                     factorNameToParameterName;

  private FactorSpaceTranslator(
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

  public FactorSpaceTranslator(FactorSpace factorSpace) {
    this(NAME_RESOLVER,
        (id) -> "0",
        (id) -> factorSpace.getFactors().get(id),
        (ii) -> (j) -> factorSpace.getFactors().get(ii).getLevels().get(j),
        (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
        factorSpace.getFactors().size());
  }

  public String parameterNameOf(int parameterId) {
    return this.name.apply(parameterId);
  }

  public String parameterTypeOf(int parameterId) {
    return this.type.apply(parameterId);
  }

  public Factor factorFor(int parameterId) {
    return factor.apply(parameterId);
  }

  public Object factorLevelOf(int parameterId, int j) {
    return this.value.apply(parameterId).apply(j);
  }

  public int numParameters() {
    return this.numParameters;
  }
}
