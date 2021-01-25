package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.unmodifiableList;

public class Pict {
  private final FactorSpace factorSpace;
  private final List<Tuple> testCases;
  private final String      algorithm;
  private final String      constraintHandler;
  private final String      mode;

  private static final Logger LOGGER = LoggerFactory.getLogger(Pict.class);
  private final        int    strength;
  private final        File   baseDir;

  private Pict(FactorSpace factorSpace, List<Tuple> testCases, int strength, File baseDir, String algorithm, String mode, String constraintHandler) {
    this.factorSpace = factorSpace;
    this.testCases = unmodifiableList(new ArrayList<>(testCases));
    this.strength = strength;
    this.baseDir = new File(baseDir, Objects.toString(Thread.currentThread().getId()));
    this.algorithm = algorithm;
    this.mode = mode;
    this.constraintHandler = constraintHandler;
  }
}
