package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.NormalizableConstraint;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.crest.utils.printable.Functions.stream;
import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils.newLine;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum PictUtils {
  ;

  public static List<Tuple> readTestSuiteFromTsv(Stream<String> data) {
    return IoUtils.readTestSuiteFromXsv(data, "\t");
  }

  public static String buildPictModel(FactorSpace factorSpace) {
    StringBuilder b = new StringBuilder();
    FactorSpaceNormalizer factorSpaceNormalizer = new FactorSpaceNormalizer(factorSpace);
    renderParameters(b, factorSpaceNormalizer);
    if (factorSpace.relationStrength() >= 0)
      renderSubmodels(b, factorSpace, factorSpaceNormalizer);
    renderConstraints(b, castToNormalizableConstraints(requireConstraintsAreAllNormalizable(factorSpace.getConstraints())), factorSpaceNormalizer);
    return b.toString();
  }

  private static void beginParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void endParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void beginConstraints(StringBuilder b) {
    b.append(newLine());
  }

  private static void endConstraints(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void renderSubmodels(StringBuilder b, FactorSpace factorSpace, FactorSpaceNormalizer factorSpaceNormalizer) {
    beginSubmodels(b, factorSpace);
    for (Submodel each : submodels(factorSpace, factorSpaceNormalizer))
      renderSubmodel(b, each);
    endSubmodels(b, factorSpace);
  }

  private static void beginSubmodels(StringBuilder b, @SuppressWarnings("unused") FactorSpace factorSpace) {
    b.append(newLine());
  }

  private static void renderSubmodel(StringBuilder b, Submodel submodel) {
    b.append(submodel.factorNames().stream().collect(joining(", ", "{ ", " }")))
        .append(" @ ")
        .append(submodel.strength());
  }

  private static void endSubmodels(@SuppressWarnings("unused") StringBuilder b, @SuppressWarnings("unused") FactorSpace factorSpace) {
  }

  private static List<Submodel> submodels(FactorSpace factorSpace, FactorSpaceNormalizer factorSpaceNormalizer) {
    return asList(
        Submodel.create(
            factorSpace.relationStrength(),
            IntStream.range(0, factorSpaceNormalizer.numNormalizedFactors() / 2)
                .mapToObj(factorSpaceNormalizer::normalizedFactorNameOf)
                .collect(toList())
        ),
        Submodel.create(
            factorSpace.relationStrength(),
            IntStream.range(factorSpaceNormalizer.numNormalizedFactors() / 2, factorSpaceNormalizer.numNormalizedFactors())
                .mapToObj(factorSpaceNormalizer::normalizedFactorNameOf)
                .collect(toList())));
  }

  private static void renderParameters(StringBuilder b, FactorSpaceNormalizer factorSpaceNormalizer) {
    beginParameters(b);
    for (int i = 0; i < factorSpaceNormalizer.numNormalizedFactors(); i++) {
      renderParameter(b, i, factorSpaceNormalizer);
    }
    endParameters(b);
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceNormalizer factorSpaceNormalizer) {
    b.append(String.format("%s:\t", factorSpaceNormalizer.normalizedFactorNameOf(i)));
    b.append(
        IntStream.range(0, factorSpaceNormalizer.numLevelsOfNormalizedFactor(i))
            .mapToObj(j -> factorSpaceNormalizer.normalizedFactorLevelOf(i, j))
            .map(Object::toString)
            .collect(joining(",")));
    b.append(newLine());
  }

  private static void renderConstraints(StringBuilder b, List<NormalizableConstraint> constraints, FactorSpaceNormalizer factorSpaceNormalizer) {
    beginConstraints(b);
    constraints
        .stream()
        .peek(each -> b.append(new PictConstraintRenderer(factorSpaceNormalizer).render(each)))
        .forEach(each -> b.append(";").append(newLine()));
    endConstraints(b);
  }

  private static List<NormalizableConstraint> castToNormalizableConstraints(List<Constraint> constraints) {
    return constraints
        .stream()
        .map(each -> (NormalizableConstraint) each)
        .collect(toList());
  }

  private static List<Constraint> requireConstraintsAreAllNormalizable(List<Constraint> constraints) {
    return require(constraints, transform(stream()).check(allMatch(isInstanceOf(NormalizableConstraint.class))));
  }

  interface Submodel {
    int strength();

    List<String> factorNames();

    static Submodel create(int strength, List<String> factorNames) {
      return new Submodel() {
        @Override
        public int strength() {
          return strength;
        }

        @Override
        public List<String> factorNames() {
          return factorNames;
        }
      };
    }
  }
}
