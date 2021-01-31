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
    beginParameters(b);
    renderParameters(b, factorSpaceNormalizer);
    endParameters(b);
    beginConstraints(b);
    renderConstraints(b, castToNormalizableConstraints(requireConstraintsAreAllNomalizable(factorSpace.getConstraints())), factorSpaceNormalizer);
    endConstraints(b);
    return b.toString();
  }

  private static void beginParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void endParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void beginConstraints(StringBuilder b) {
    ;
    b.append(newLine());
  }

  private static void endConstraints(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void renderParameters(StringBuilder b, FactorSpaceNormalizer factorSpaceNormalizer) {
    for (int i = 0; i < factorSpaceNormalizer.numNormalizedFactors(); i++) {
      renderParameter(b, i, factorSpaceNormalizer);
    }
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceNormalizer factorSpaceNormalizer) {
    b.append(String.format("%s:\t", factorSpaceNormalizer.normalizedFactorNameOf(i) ));
    b.append(
        IntStream.range(0, factorSpaceNormalizer.numLevelsOfNormalizedFactor(i))
            .mapToObj(j -> factorSpaceNormalizer.normalizedFactorLevelOf(i, j))
            .map(Object::toString)
            .collect(joining(",")));
    b.append(newLine());
  }

  private static void renderConstraints(StringBuilder b, List<NormalizableConstraint> constraints, FactorSpaceNormalizer factorSpaceNormalizer) {
    constraints
        .stream()
        .peek(each -> b.append(new PictConstraintRenderer(factorSpaceNormalizer).render(each)))
        .forEach(each -> b.append(";").append(newLine()));
  }

  private static List<NormalizableConstraint> castToNormalizableConstraints(List<Constraint> constraints) {
    return constraints
        .stream()
        .map(each -> (NormalizableConstraint) each)
        .collect(toList());
  }

  private static List<Constraint> requireConstraintsAreAllNomalizable(List<Constraint> constraints) {
    return require(constraints, transform(stream()).check(allMatch(isInstanceOf(NormalizableConstraint.class))));
  }
}
