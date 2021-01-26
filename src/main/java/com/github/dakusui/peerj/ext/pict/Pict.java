package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.ExternalEngine;

import java.io.File;
import java.util.List;

public class Pict extends ExternalEngine.Base {
  private Pict(FactorSpace factorSpace, List<Tuple> testCases, int strength, File baseDir, GenerationMode mode) {
    super(baseDir, strength, factorSpace, mode, testCases);
  }

  @Override
  public String engineName() {
    return "pict";
  }

  @Override
  public String composeCommandLine(File inFile, File outFile) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String buildModel() {
    throw new UnsupportedOperationException();
  }
}
