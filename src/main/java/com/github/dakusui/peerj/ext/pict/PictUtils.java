package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.shared.FactorSpaceTranslator;
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
    FactorSpaceTranslator factorSpaceTranslator = new FactorSpaceTranslator(factorSpace);
    beginParameters(b);
    renderParameters(b, factorSpaceTranslator);
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

  private static void renderParameters(StringBuilder b, FactorSpaceTranslator factorSpaceTranslator) {
    for (int i = 0; i < factorSpaceTranslator.numFactors(); i++) {
      renderParameter(b, i, factorSpaceTranslator);
    }
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceTranslator factorSpaceTranslator) {
    b.append(String.format("%-20s", factorSpaceTranslator.formalFactorNameOf(i)));
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
