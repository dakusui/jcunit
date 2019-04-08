package com.github.dakusui.jcunit8.experiments.join;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import com.github.dakusui.printables.PrintablePredicate;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum ActsUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(ActsUtils.class);

  static String buildActsModel(FactorSpace factorSpace, String systemName) {
    StringBuilder b = new StringBuilder();
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    b.append("<System name=\"").append(systemName).append("\">\n");
    b.append("  <Parameters>\n");
    for (int i = 0; i < factorSpace.getFactors().size(); i++) {
      b.append("    <Parameter id=\"").append(i).append("\" name=\"")
          .append(String.format("PREFIX-%d", i))
          .append("\" type=\"0\">\n");
      b.append("      <values>\n");
      Factor factor = factorSpace.getFactors().get(i);
      for (int j = 0; j < factor.getLevels().size(); j++) {
        b.append("        <value>").append(factor.getLevels().get(j)).append("</value>\n");
      }
      b.append("      </values>\n");
      b.append("      <basechoices />\n").append("      <invalidValues />\n")
          .append("    </Parameter>\n");
    }
    b.append("  </Parameters>\n");
    b.append("</System>");

    return b.toString();
  }

  static List<Tuple> readTestSuiteFromCsv(Stream<String> data) {
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

  private static void runActs(File baseDir, int strength) {
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
    processStreamer(StableTemplatingUtils.template(
        "{{JAVA}} -Ddoi={{STRENGTH}} -Dalgo={{ALGORITHM}} -Dchandler={{CHANDLER}} -Doutput=csv -jar {{ACTS_JAR}} {{IN}} {{OUT}}",
        new TreeMap<String, Object>() {{
          put("{{JAVA}}", "java");
          put("{{STRENGTH}}", strength);
          put("{{ALGORITHM}}", "ipog");
          put("{{CHANDLER}}", "solver");
          put("{{ACTS_JAR}}", actsJar());
          put("{{IN}}", inFile(baseDir));
          put("{{OUT}}", outFile(baseDir));
        }}), new ProcessStreamer.Checker() {
      private StreamChecker stdoutChecker = createStreamChecker();
      private StreamChecker stderrChecker = createStreamChecker();

      private StreamChecker createStreamChecker() {
        return new StreamChecker() {
          boolean errorFound = false;

          @Override
          public boolean getAsBoolean() {
            return !errorFound;
          }

          @Override
          public void accept(String s) {
            if (s.contains("Errors encountered"))
              errorFound = true;
          }

          @Override
          public String toString() {
            return "A record that starts with 'Errors encountered' was found.";
          }
        };
      }

      @Override
      public StreamChecker forStdOut() {
        return stdoutChecker;
      }

      @Override
      public StreamChecker forStdErr() {
        return stderrChecker;
      }

      @Override
      public Predicate<Integer> exitCodeChecker() {
        return new PrintablePredicate.Builder<Integer>(i -> Objects.equals(i, 0)).describe("==[0]");
      }
    })
        .stream()
        .forEach(LOGGER::trace);
  }

  private static File outFile(File baseDir) {
    return new File(baseDir, "acts.ca");
  }

  private static File inFile(File baseDir) {
    return new File(baseDir, "acts.xml");
  }

  private static String actsJar() {
    return "src/test/resources/bin/acts_3.0.jar";
  }

  private static Stream<String> streamFile(File file) {
    return processStreamer(String.format("cat %s", file.getAbsolutePath()), ProcessStreamer.Checker.createDefault()).stream();
  }

  private static ProcessStreamer processStreamer(String command, ProcessStreamer.Checker checker) {
    LOGGER.debug("Executing:[{}]", command);
    return new ProcessStreamer.Builder(Shell.local(), command)
        .checker(checker)
        .build();
  }

  private static void writeTo(File file, String data) {
    processStreamer(String.format("echo '%s' > %s", data, file.getAbsolutePath()), ProcessStreamer.Checker.createDefault())
        .stream()
        .forEach(LOGGER::debug);
  }

  public static List<Tuple> generateWithActs(File baseDir, FactorSpace factorSpace, int strength) {
    LOGGER.debug("Directory:{} was created: {}", baseDir, baseDir.mkdirs());
    writeTo(inFile(baseDir), buildActsModel(factorSpace, "unknown"));
    runActs(baseDir, strength);
    try (Stream<String> s = streamFile(outFile(baseDir))) {
      return readTestSuiteFromCsv(s);
    }
  }

  public static void main(String... args) {
    File baseDir = new File("target");
    generateAndReport(baseDir, 2, 90, 3);
    generateAndReport(baseDir, 2, 180, 3);
  }

  public static void generateAndReport(File baseDir, int numLevels, int numFactors, int strength) {
    CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
    List<Tuple> generated;
    generated = generateWithActs(baseDir, numLevels, numFactors, strength);
    System.out.println("size=" + generated.size() + " time=" + stopWatch.get() + "[msec]");
  }

  public static List<Tuple> generateWithActs(File baseDir, int numLevels, int numFactors, int strength) {
    generateWithActs(
        baseDir,
        new FactorSpaceSpec("L").addFactor(numLevels, numFactors).build(),
        strength);
    List<Tuple> ret = new LinkedList<>();
    try (Stream<String> data = streamFile(outFile(baseDir))) {
      ret.addAll(readTestSuiteFromCsv(data));
    }
    return ret;
  }

  private static Stream<String> readCsv() {
    return Stream.of(
        "# ACTS Test Suite Generation: Sun Apr 07 17:57:26 JST 2019",
        "#  '*' represents don't care value ",
        "# Degree of interaction coverage: ",
        "# Number of parameters: 10",
        "# Maximum number of values per parameter: 2",
        "# Number ofconfigurations: 10",
        "PREFIX-0,PREFIX-1,PREFIX-2,PREFIX-3,PREFIX-4,PREFIX-5,PREFIX-6,PREFIX-7,PREFIX-8,PREFIX-9",
        "0,0,1,1,1,1,1,1,1,1",
        "0,1,0,0,0,0,0,0,0,0",
        "1,0,0,1,0,1,0,1,0,1",
        "1,1,1,0,1,0,1,0,1,0",
        "1,0,1,0,0,1,0,0,1,1",
        "1,1,0,1,1,0,1,1,0,0",
        "0,0,1,1,1,0,0,0,0,1",
        "1,1,0,0,0,1,1,1,1,0",
        "1,0,0,1,0,1,1,0,0,0",
        "0,1,1,1,1,1,0,1,0,1");
  }
}
