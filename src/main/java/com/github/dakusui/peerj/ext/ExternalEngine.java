package com.github.dakusui.peerj.ext;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.acts.ActsUtils;
import com.github.dakusui.peerj.ext.shared.IoUtils;
import com.github.dakusui.peerj.utils.ProcessStreamerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.ext.ExternalEngine.GenerationMode.INCREMENTAL;
import static com.github.dakusui.peerj.ext.ExternalEngine.GenerationMode.SCRATCH;
import static com.github.dakusui.peerj.ext.shared.IoUtils.writeTo;
import static com.github.dakusui.peerj.utils.ProcessStreamerUtils.streamFile;

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
    writeTo(new File(baseDir(), executionTimeFile), String.format("%s[msec]", executionTimeInMillis));
  }

  default List<Tuple> run() {
    final File inFile = IoUtils.inFile(baseDir());
    boolean baseDirCreated = baseDir().mkdirs();
    LOGGER.debug("Basedir was created: {}", baseDirCreated);
    IoUtils.writeTo(inFile, Arrays.stream(buildModel().split("\n")));
    final File outFile = IoUtils.outFile(baseDir());
    String commandLine = composeCommandLine(inFile, outFile);
    recordExecutedCommandLine(commandLine, commandLineFile());
    long before = System.currentTimeMillis();
    ProcessStreamerUtils.processStreamer(
        commandLine,
        new ProcessStreamerUtils.StandardChecker("Errors encountered", "Constraints can not be parsed"))
        .stream()
        .forEach(LOGGER::trace);
    recordExecutionTime(System.currentTimeMillis() - before, executionTimeFile());
    try (Stream<String> s = streamFile(outFile).peek(LOGGER::trace)) {
      return ActsUtils.readTestSuiteFromCsv(s);
    }
  }

  default String executionTimeFile() {
    return engineName() + ".time";
  }

  default String commandLineFile() {
    return engineName() + ".commandLine";
  }

  String engineName();


  File baseDir();

  String buildModel();

  List<Tuple> testCases();

  FactorSpace factorSpace();

  int strength();

  GenerationMode generationMode();

  String composeCommandLine(File inFile, File outFile);

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
      this.baseDir = baseDir;
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
