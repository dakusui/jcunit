package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.IoUtils;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.crest.utils.printable.Functions.stream;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static com.github.dakusui.peerj.ext.base.IoUtils.newLine;
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
    beginConstraints(b, castToNormalizableConstraints(requireConstraintsAreAllNomalizable(factorSpace.getConstraints())), factorSpaceNormalizer);
    renderConstraints(b);
    endConstraints(b);
    return b.toString();
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

  private static void beginParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void endParameters(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void beginConstraints(StringBuilder b, List<NormalizableConstraint> constraints, FactorSpaceNormalizer factorSpaceNormalizer) {
    ;
    b.append(newLine());

    constraints
        .stream()
        .peek(each -> b.append(new PictConstraintRenderer(factorSpaceNormalizer).render(each)))
        .forEach(each -> b.append(";").append(newLine()));
  }

  private static void endConstraints(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void renderParameters(StringBuilder b, FactorSpaceNormalizer factorSpaceNormalizer) {
    for (int i = 0; i < factorSpaceNormalizer.numNormalizedFactors(); i++) {
      renderParameter(b, i, factorSpaceNormalizer);
    }
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceNormalizer factorSpaceNormalizer) {
    b.append(String.format("%-20s", factorSpaceNormalizer.normalizedFactorNameOf(i)));
  }

  private static void renderConstraints(StringBuilder b) {

  }

  static class PictModelRenderer {
    final StringBuilder b;

    PictModelRenderer() {
      b = new StringBuilder();
    }

    String render() {
      return b.toString();
    }
  }
}
