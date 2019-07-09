package com.github.dakusui.jcunit8.extras.generators;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.ProcessStreamerUtils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.ProcessStreamerUtils.streamFile;
import static com.github.dakusui.jcunit.core.utils.ProcessStreamerUtils.writeTo;
import static com.github.dakusui.jcunit8.extras.generators.ActsUtils.buildActsModel;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class Acts {

  private final FactorSpace factorSpace;
  private       String      algorithm;
  private       String      constraintHandler;

  private static List<Tuple> runActs(File baseDir, FactorSpace factorSpace, int strength) {
    LOGGER.debug("Directory:{} was created: {}", baseDir, baseDir.mkdirs());
    return new Acts.Builder().baseDir(baseDir).factorSpace(factorSpace).strength(strength).build().run();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(Acts.class);
  private final        int    strength;
  private final        File   baseDir;

  private Acts(FactorSpace factorSpace, int strength, File baseDir, String algorithm, String constraintHandler) {
    this.factorSpace = factorSpace;
    this.strength = strength;
    this.baseDir = baseDir;
    this.algorithm = algorithm;
    this.constraintHandler = constraintHandler;
  }

  public static List<Tuple> readTestSuiteFromCsv(Stream<String> data) {
    AtomicReference<List<String>> header = new AtomicReference<>();
    return data.filter(s -> !s.startsWith("#"))
        .filter(s -> {
          if (header.get() == null) {
            header.set(asList(s.split(",")));
            return false;
          }
          return true;
        })
        .map(
            s -> {
              List<String> record = asList(s.split(","));
              List<String> h = header.get();
              if (record.size() != h.size()) {
                System.out.println("header:" + h);
                System.out.println("record:" + record);
                throw new IllegalArgumentException("size(header)=" + h.size() + ", size(record)=" + record.size());
              }
              Tuple.Builder b = Tuple.builder();
              for (int i = 0; i < h.size(); i++)
                b.put(h.get(i), record.get(i));
              return b.build();
            }
        )
        .collect(toList());
  }

  static File outFile(File baseDir) {
    return new File(baseDir, "acts.ca");
  }

  private static File inFile(File baseDir) {
    return new File(baseDir, "acts.xml");
  }

  private static String actsJar() {
    return "src/test/resources/bin/acts_3.0.jar";
  }

  public static List<Tuple> generateWithActs(File baseDir, FactorSpace factorSpace, int strength) {
    return runActs(baseDir, factorSpace, strength);
  }

  private List<Tuple> run() {
    writeTo(inFile(baseDir), buildActsModel(factorSpace, "unknown"));
    /*
      ACTS Version: 3.0
      Usage: java [options] -jar jarName <inputFileName> [outputFileName]
      where options include:
      -Dalgo=ipog|ipog_d|ipof|ipof2|basechoice|null
               ipog - use algorithm IPO (default)
               ipog_d - use algorithm IPO + Binary Construction (for large systems)
               ipof - use ipof method
               ipof2 - use the ipof2 method
               basechoice - use Base Choice method
               null - use to check coverage only (no test generation)
      -Ddoi=<int>
               specify the degree of interactions to be covered. Use -1 for mixed strength.
      -Doutput=numeric|nist|csv|excel
               numeric - output test set in numeric format
               nist - output test set in NIST format (default)
               csv - output test set in CSV format
               excel - output test set in EXCEL format
      -Dmode=scratch|extend
               scratch - generate tests from scratch (default)
               extend - extend from an existing test set
      -Dchandler=no|solver|forbiddentuples
               no - ignore all constraints
               solver - handle constraints using CSP solver
               forbiddentuples - handle constraints using minimum forbidden tuples (default)
     */
    ProcessStreamerUtils.processStreamer(StableTemplatingUtils.template(
        "{{JAVA}} -Ddoi={{STRENGTH}} -Dalgo={{ALGORITHM}} -Dchandler={{CHANDLER}} -Doutput=csv -jar {{ACTS_JAR}} {{IN}} {{OUT}}",
        new TreeMap<String, Object>() {{
          put("{{JAVA}}", "java");
          put("{{STRENGTH}}", strength);
          put("{{ALGORITHM}}", algorithm);
          put("{{CHANDLER}}", constraintHandler);
          put("{{ACTS_JAR}}", actsJar());
          put("{{IN}}", inFile(baseDir));
          put("{{OUT}}", outFile(baseDir));
        }}), new ProcessStreamerUtils.StandardChecker("Errors encountered", "Constraints can not be parsed"))
        .stream()
        .forEach(LOGGER::trace);

    try (Stream<String> s = streamFile(outFile(baseDir)).peek(LOGGER::trace)) {
      return readTestSuiteFromCsv(s);
    }
  }

  public static class Builder {
    private File        baseDir;
    private int         strength          = 2;
    private String      algorithm         = "ipog";
    private String      constraintHandler = "solver";
    private FactorSpace factorSpace;

    public Acts build() {
      return new Acts(
          requireNonNull(factorSpace),
          strength, baseDir, algorithm, constraintHandler);
    }

    public Builder baseDir(File baseDir) {
      this.baseDir = requireNonNull(baseDir);
      return this;
    }

    public Builder strength(int strength) {
      this.strength = strength;
      return this;
    }

    /**
     * You can set one of {@code no}, {@code solver}, and {@code forbiddentuples}, which represent
     * "No constraint handler is used", "CSP solver", adn "Minimum forbidden tuples method", respectively.
     *
     * @param constraintHandler A constraint handler used during covering array generation.
     * @return Thi object.
     */
    public Builder constraintHandler(String constraintHandler) {
      this.constraintHandler = requireNonNull(constraintHandler);
      return this;
    }

    /**
     * Sets a name of an algorithm with which a covering array is generated.
     * You can use one of followings.
     * <ul>
     * <li>ipog - use algorithm IPO (default)</li>
     * <li>ipog_d - use algorithm IPO + Binary Construction (for large systems)</li>
     * <li>ipof - use ipof method</li>
     * <li>ipof2 - use the ipof2 method</li>
     * <li>basechoice - use Base Choice method</li>
     * <li>null - use to check coverage only (no test generation)</li>
     * </ul>
     *
     * @param algorithm A name of algorithm used for covering array generation.
     * @return This object
     */
    public Builder algorithm(String algorithm) {
      this.algorithm = requireNonNull(algorithm);
      return this;
    }

    public Builder factorSpace(FactorSpace factorSpace) {
      this.factorSpace = requireNonNull(factorSpace);
      return this;
    }
  }

}
