package com.github.dakusui.peerj.model;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class FactorSpaceSpecForExperiments extends FactorSpaceSpec {
  protected final String                                             prefix;
  private         String                                             constraintSetName = null;
  protected final List<Function<List<String>, NormalizedConstraint>> constraints       = new LinkedList<>();

  public FactorSpaceSpecForExperiments(String prefix) {
    this.prefix = prefix;
  }

  public FactorSpaceSpecForExperiments addFactors(int numLevels, int numFactors) {
    FactorSpaceSpecForExperiments ret = this;
    for (int i = 0; i < numFactors; i++)
      ret = this.addFactor(numLevels);
    return ret;
  }

  public FactorSpaceSpecForExperiments addFactor(int numLevels) {
    this.factorSpecs.putIfAbsent(numLevels, 0);
    this.factorSpecs.put(numLevels, factorSpecs.get(numLevels) + 1);
    return this;
  }

  @Override
  public String createSignature() {
    return super.createSignature() +
        constraintSetName()
            .map(n -> "-" + n)
            .orElse("");
  }

  public FactorSpaceSpecForExperiments constraintSetName(String constraintSetName) {
    this.constraintSetName = constraintSetName;
    return this;
  }

  public Optional<String> constraintSetName() {
    if (constraints.isEmpty())
      return Optional.empty();
    return Optional.of(constraintSetName);
  }

  public FactorSpaceSpecForExperiments addConstraint(Function<List<String>, NormalizedConstraint> constraint) {
    this.constraints.add(constraint);
    return this;
  }

  public List<Function<List<String>, NormalizedConstraint>> constraints() {
    return constraints;
  }

  public FactorSpace build() {
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
            .collect(toList()));
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
