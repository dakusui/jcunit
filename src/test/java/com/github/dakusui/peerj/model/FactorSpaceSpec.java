package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class FactorSpaceSpec {
  /**
   * Descending order by the number of levels of factors.
   */
  protected final SortedMap<Integer, Integer>       factorSpecs           = new TreeMap<>((o1, o2) -> o2 - o1);
  private final   Map<String, String>               normalizedFactorNames = new HashMap<>();
  private final   Map<String, String>               rawFactorNames = new HashMap<>();
  private final   SortedSet<NormalizableConstraint> constraints    = new TreeSet<>(Comparator.reverseOrder());

  public FactorSpaceSpec addFactor(Factor factor) {
    checkcond(!normalizedFactorNames.containsKey(factor.getName()));
    int numLevels = factor.getLevels().size();
    String normalizedFactorName = format("p%sn%s", numLevels, factorSpecs.get(numLevels));
    normalizedFactorNames.put(factor.getName(), normalizedFactorName);
    rawFactorNames.put(normalizedFactorName, factor.getName());
    this.factorSpecs.putIfAbsent(numLevels, 0);
    this.factorSpecs.put(numLevels, factorSpecs.get(numLevels) + 1);
    return this;
  }

  public String createSignature() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(","));
  }

  public String toString() {
    return createSignature();
  }

  public int numFactors() {
    return this.factorSpecs.values().stream().reduce(Integer::sum).orElse(0);
  }
}
