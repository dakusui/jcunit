package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.pict;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.ExternalEngine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils.newLine;
import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.stream.Collectors.joining;

public class Pict extends ExternalEngine.Base {
  private Pict(FactorSpace factorSpace, List<Tuple> testCases, int strength, File baseDir) {
    super(baseDir, strength, factorSpace, testCases);
  }

  @Override
  public String engineName() {
    return "pict";
  }

  @Override
  public String buildSeedData(List<Tuple> seedTestCases) {
    require(seedTestCases, isNotNull());
    return Stream.concat(
        Stream.of(this.factorSpace().getFactorNames()),
        seedTestCases.stream().map(Map::values))
        .map(each -> each.stream()
            .map(Object::toString)
            .collect(joining("\t")))
        .collect(joining(newLine()));
  }

  @Override
  public String composeCommandLine(File inFile, File outFile, File seedFile) {
    return StableTemplatingUtils.template(
        "\"{{PICT_EXEC}}\" {{IN}} /o:{{STRENGTH}} /c > {{OUT}}",
        new TreeMap<String, Object>() {{
          put("{{PICT_EXEC}}", pathToBinary());
          if (generationMode() == GenerationMode.INCREMENTAL)
            put("{{SEED}}", seedFile);
          put("{{STRENGTH}}", strength());
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

  @Override
  public String seedFilename(String engineName) {
    return engineName + ".seed.tsv";
  }

  @Override
  public String outputCoveringArrayFilename(String engineName) {
    return engineName + ".out.tsv";
  }

  @Override
  public String modelFilename(final String engineName) {
    return engineName + ".model";
  }

  private static String pathToBinary() {
    return "src/test/resources/bin/" + System.getProperty("os.name") + "/pict";
  }

  public static List<Tuple> runPict(File baseDir, FactorSpace factorSpace, int strength, List<Tuple> testCases) {
    return new Pict(factorSpace, testCases, strength, baseDir).run();
  }
}
