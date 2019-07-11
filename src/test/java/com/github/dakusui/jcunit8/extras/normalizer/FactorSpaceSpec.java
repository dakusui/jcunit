package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.extras.normalizer.compat.NormalizedConstraint;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class FactorSpaceSpec {
  protected final SortedMap<Integer, Integer> factorSpecs = new TreeMap<>((o1, o2) -> o2 - o1);
  protected final List<Function<List<String>, NormalizedConstraint>> constraints = new LinkedList<>();

  public FactorSpaceSpec addFactor(Factor factor) {
    return this;
  }

  FactorSpaceSpec addConstraint(Constraint constraint) {
    return this;
  }

  String normalizeRawFactorName(String rawFactorName) {
    return null;
  }

  String denormalizeFactorName(String rawFactorName) {
    return null;
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

  public Stream<Map.Entry<Integer, Integer>> factorSpecs() {
    return this.factorSpecs.entrySet().stream();
  }

}
