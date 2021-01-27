package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.shared.FactorSpaceAdapter;
import com.github.dakusui.peerj.ext.shared.IoUtils;

import java.util.List;
import java.util.stream.Stream;

public enum PictUtils {
  ;

  public static List<Tuple> readTestSuiteFromTsv(Stream<String> data) {
    return IoUtils.readTestSuiteFromXsv(data, "\t");
  }

  public static String buildPictModel(FactorSpace factorSpace) {
    StringBuilder b = new StringBuilder();
    FactorSpaceAdapter factorSpaceAdapter = new FactorSpaceAdapter(factorSpace);
    beginParameters(b);
    renderParameters(b, factorSpaceAdapter);
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
    b.append(IoUtils.newLine());
  }

  private static void endConstraints(@SuppressWarnings("unused") StringBuilder b) {
  }

  private static void renderParameters(StringBuilder b, FactorSpaceAdapter factorSpaceAdapter) {
    for (int i = 0; i < factorSpaceAdapter.numParameters; i++) {
      renderParameter(b, i, factorSpaceAdapter);
    }
  }

  private static void renderParameter(StringBuilder b, int i, FactorSpaceAdapter factorSpaceAdapter) {
    b.append(String.format("%-20s", factorSpaceAdapter.factorNameToParameterName.apply(factorSpaceAdapter.factor.apply(i).getName())));
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
