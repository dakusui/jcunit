package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.ExternalEngine;
import com.github.dakusui.peerj.ext.shared.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

public class Pict implements ExternalEngine {
  private final FactorSpace factorSpace;
  private final List<Tuple> testCases;
  private final String      algorithm;
  private final String      mode;

  private static final Logger LOGGER = LoggerFactory.getLogger(Pict.class);
  private final        int    strength;
  private final        File   baseDir;

  private Pict(FactorSpace factorSpace, List<Tuple> testCases, int strength, File baseDir, String algorithm, String mode) {
    this.factorSpace = factorSpace;
    this.testCases = unmodifiableList(new ArrayList<>(testCases));
    this.strength = strength;
    this.baseDir = new File(baseDir, Objects.toString(Thread.currentThread().getId()));
    this.algorithm = algorithm;
    this.mode = mode;
  }

  @Override
  public String engineName() {
    return "pict";
  }

  @Override
  public File baseDir() {
    return this.baseDir;
  }

  @Override
  public List<Tuple> testCases() {
    return this.testCases;
  }

  @Override
  public FactorSpace factorSpace() {
    return this.factorSpace;
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
