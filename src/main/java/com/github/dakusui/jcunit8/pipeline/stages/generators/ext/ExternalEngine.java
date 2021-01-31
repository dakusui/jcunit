package com.github.dakusui.jcunit8.pipeline.stages.generators.ext;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.utils.ProcessStreamerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.ExternalEngine.GenerationMode.INCREMENTAL;
import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.ExternalEngine.GenerationMode.SCRATCH;
import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils.writeTo;
import static com.github.dakusui.peerj.utils.ProcessStreamerUtils.streamFile;
import static java.lang.String.format;

public interface ExternalEngine {
  enum GenerationMode {
    SCRATCH,
    INCREMENTAL
  }

  Logger LOGGER = LoggerFactory.getLogger(ExternalEngine.class);

  default void recordExecutedCommandLine(String commandLine, String commandLineFile) {
    writeTo(new File(baseDir(), commandLineFile), commandLine);
  }

  default void recordExecutionTime(long executionTimeInMillis, String executionTimeFile) {
    writeTo(new File(baseDir(), executionTimeFile), format("%s[msec]", executionTimeInMillis));
  }

  default List<Tuple> run() {
    final File seedFile = prepareSeedFile();
    final File inFile = prepareModelFile();
    final File outFile = prepareOutputFile();
    String commandLine = composeCommandLine(inFile, outFile, seedFile);
    recordExecutedCommandLine(commandLine, commandLineFile());
    long before = System.currentTimeMillis();
    ProcessStreamerUtils.processStreamer(
        commandLine,
        new ProcessStreamerUtils.StandardChecker("Errors encountered", "Constraints can not be parsed"))
        .stream()
        .forEach(LOGGER::trace);
    recordExecutionTime(System.currentTimeMillis() - before, executionTimeFile());
    try (Stream<String> s = streamFile(outFile).peek(LOGGER::trace)) {
      return readTestSuiteFromStream(s);
    }
  }

  default File prepareSeedFile() {
    final File seedFile = seedFile();
    if (isSeedFileRequired()) {
      boolean baseDirCreated = baseDir().mkdirs();
      LOGGER.debug("Basedir for seed file was created: {}", baseDirCreated);
      writeTo(seedFile, Arrays.stream(buildSeedData(this.testCases()).split("\n")));
    }
    return seedFile;
  }

  default boolean isSeedFileRequired() {
    return this.generationMode() == INCREMENTAL;
  }

  default File prepareModelFile() {
    final File inFile = modelFile();
    boolean baseDirCreated = baseDir().mkdirs();
    LOGGER.debug("Basedir for model file was created: {}", baseDirCreated);
    writeTo(inFile, Arrays.stream(buildModel().split("\n")));
    return inFile;
  }

  default File prepareOutputFile() {
    final File outFile = outputCoveringArrayFile();
    boolean baseDirCreated = baseDir().mkdirs();
    LOGGER.debug("Basedir for output file was created: {}", baseDirCreated);
    writeTo(outFile, Arrays.stream(buildModel().split("\n")));
    return outFile;
  }

  default File seedFile() {
    return new File(baseDir(), seedFilename(engineName()));
  }

  default File outputCoveringArrayFile() {
    return new File(baseDir(), outputCoveringArrayFilename(engineName()));
  }

  default File modelFile() {
    return new File(baseDir(), modelFilename(engineName()));
  }

  default String executionTimeFile() {
    return engineName() + ".time";
  }

  default String commandLineFile() {
    return engineName() + ".commandLine";
  }

  String seedFilename(String engineName);

  String outputCoveringArrayFilename(String engineName);

  String modelFilename(final String engineName);

  List<Tuple> readTestSuiteFromStream(Stream<String> s);

  String engineName();

  File baseDir();

  String buildSeedData(List<Tuple> seedTestCases);

  String buildModel();

  List<Tuple> testCases();

  FactorSpace factorSpace();

  int strength();

  GenerationMode generationMode();

  String composeCommandLine(File inFile, File outFile, File seedFile);

  abstract class Base implements ExternalEngine {
    private final FactorSpace    factorSpace;
    private final List<Tuple>    testCases;
    private final int            strength;
    private final File           baseDir;
    private final GenerationMode generationMode;

    protected Base(File baseDir, int strength, FactorSpace factorSpace, List<Tuple> testCases) {
      this(baseDir, strength, factorSpace, testCases.isEmpty() ? SCRATCH : INCREMENTAL, testCases);
    }

    protected Base(File baseDir, int strength, FactorSpace factorSpace, GenerationMode generationMode, List<Tuple> testCases) {
      this.factorSpace = factorSpace;
      this.testCases = testCases;
      this.strength = strength;
      this.baseDir = new File(baseDir, format("%s", Thread.currentThread().getId()));
      this.generationMode = generationMode;
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
    public int strength() {
      return this.strength;
    }

    @Override
    public GenerationMode generationMode() {
      return this.generationMode;
    }
  }
}
