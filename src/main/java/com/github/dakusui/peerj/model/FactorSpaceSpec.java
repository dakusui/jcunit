package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FactorSpaceSpec {
  protected final String                                             prefix;
  private         String                                               constraintSetName = null;
  protected final List<Function<List<String>, NormalizableConstraint>> constraints       = new LinkedList<>();
  /**
   * Descending order by the number of levels of factors.
   */
  protected final SortedMap<Integer, Integer>                          factorSpecs       = new TreeMap<>((o1, o2) -> o2 - o1);

  /**
   * A non-negative value is set to experiment VSCA generation.
   */
  protected int relationStrength = -1;
  protected int baseStrength     = -1;

  public FactorSpaceSpec(String prefix) {
    this.prefix = prefix;
  }

  public int numFactors() {
    return this.factorSpecs.values().stream().reduce(Integer::sum).orElse(0);
  }

  public FactorSpaceSpec addFactors(int numLevels, int numFactors) {
    FactorSpaceSpec ret = this;
    for (int i = 0; i < numFactors; i++)
      ret = this.addFactor(numLevels);
    return ret;
  }

  public FactorSpaceSpec addFactor(int numLevels) {
    this.factorSpecs.putIfAbsent(numLevels, 0);
    this.factorSpecs.put(numLevels, factorSpecs.get(numLevels) + 1);
    return this;
  }

  public FactorSpaceSpec baseStrength(int baseStrength) {
    this.baseStrength = baseStrength;
    return this;
  }

  public FactorSpaceSpec relationStrength(int relationStrength) {
    this.relationStrength = relationStrength;
    return this;
  }

  private String createSignaturePrivate() {
    String ret = this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(","));
    if (this.relationStrength < 0)
      return ret;
    return ret + "-relatioonStrength:" + relationStrength;
  }

  public String createSignature() {
    return createSignaturePrivate() +
        constraintSetName()
            .map(n -> "-" + n)
            .orElse("");
  }

  public FactorSpaceSpec constraintSetName(String constraintSetName) {
    this.constraintSetName = constraintSetName;
    return this;
  }

  public Optional<String> constraintSetName() {
    if (constraints.isEmpty())
      return Optional.empty();
    return Optional.of(constraintSetName);
  }

  public FactorSpaceSpec addConstraint(Function<List<String>, NormalizableConstraint> constraint) {
    this.constraints.add(constraint);
    return this;
  }

  public List<Function<List<String>, NormalizableConstraint>> constraints() {
    return constraints;
  }

  public FactorSpace toFactorSpace() {
    AtomicInteger index = new AtomicInteger(0);
    LinkedList<Factor> factors = new LinkedList<Factor>() {{
      factorSpecs.keySet().stream()
          .flatMap((Integer numLevels) -> IntStream.range(0, factorSpecs.get(numLevels))
              .mapToObj(i -> Factor.create(
                  composeFactorName(index::getAndIncrement),
                  IntStream.range(0, numLevels)
                      .boxed().collect(toList())
                      .toArray())))
          .forEach(this::add);
    }};
    return FactorSpace.create(
        factors,
        constraints.stream()
            .map(each -> each.apply(factors.stream().map(Factor::getName).collect(toList())))
            .collect(toList()),
        this.baseStrength,
        this.relationStrength);
  }

  @Override
  public String toString() {
    return this.factorSpecs.keySet().stream()
        .map(k -> format("%s^%s", k, this.factorSpecs.get(k)))
        .collect(joining(" ", format("%s[", this.prefix()), "]"));
  }

  private String composeFactorName(IntSupplier factorId) {
    return format("%s-%02d", prefix(), factorId.getAsInt());
  }

  public Stream<Map.Entry<Integer, Integer>> factorSpecs() {
    return this.factorSpecs.entrySet().stream();
  }

  public String prefix() {
    return prefix;
  }
}
