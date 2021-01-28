package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.shared.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.shared.IoUtils;

import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.ext.shared.IoUtils.newLine;

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
    renderConstraints(b);
    endConstraints(b);
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

  private static void renderParameters(StringBuilder b, FactorSpaceNormalizer factorSpaceNormalizer) {
    for (int i = 0; i < factorSpaceNormalizer.numFormalFactors(); i++) {
      renderParameter(b, i, factorSpaceNormalizer);
    }
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceNormalizer factorSpaceNormalizer) {
    b.append(String.format("%-20s", factorSpaceNormalizer.formalFactorNameOf(i)));
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
