package com.github.dakusui.peerj.ext.pict;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.ExternalEngine;
import com.github.dakusui.peerj.ext.base.FactorSpaceNormalizer;
import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import java.io.File;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

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
    return StableTemplatingUtils.template(
        "{{PICT_EXEC}} {{IN}} /o:{{STRENGTH}} /c > {{OUT}}",
        new TreeMap<String, Object>() {{
          put("{{PICT_EXEC}}", pathToBinary());
          if (generationMode() == GenerationMode.INCREMENTAL)
            put("{{SEED}}", seedFile());
          put("{{IN}}", inFile);
          put("{{OUT}}", outFile);
        }}
    );
  }

  @Override
  public String buildModel() {
    return PictUtils.buildPictModel(factorSpace());
  }

  @Override
  public List<Tuple> readTestSuiteFromStream(Stream<String> s) {
    return PictUtils.readTestSuiteFromTsv(s);
  }


  private String seedFile() {
    throw new UnsupportedOperationException();
  }

  private static String pathToBinary() {
    return "src/test/resources/bin/" + System.getProperty("os.name") + "/pict";
  }

}
