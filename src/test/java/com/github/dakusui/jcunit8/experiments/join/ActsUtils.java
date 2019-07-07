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
    renderParameters(factorSpace, b, 1);
    renderConstraints(b, 1);
    b.append("\n");
    b.append("</System>");

    return b.toString();
  }

  private static void renderParameters(FactorSpace factorSpace, StringBuilder b, int indentLevel) {
    b.append(indent(indentLevel)).append("<Parameters>").append(newLine());
    indentLevel++;
    for (int i = 0; i < factorSpace.getFactors().size(); i++) {
      indentLevel = renderParameter(factorSpace, b, indentLevel, i);
    }
    b.append(indent(indentLevel)).append("</Parameters>").append(newLine());
  }

  private static int renderParameter(FactorSpace factorSpace, StringBuilder b, int indentLevel, int parameterId) {
    String parameterName = String.format("PREFIX-%d", parameterId);
    String parameterType = "0";
    b.append(indent(indentLevel))
        .append("<Parameter id=\"").append(parameterId).append("\" name=\"")
        .append(parameterName).append("\" type=\"").append(parameterType).append("\">")
        .append(newLine());
    indentLevel++;
    b.append(indent(indentLevel)).append("<values>").append(newLine());
    Factor factor = factorSpace.getFactors().get(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(indent(indentLevel)).append("<value>").append(factor.getLevels().get(j)).append("</value>").append(newLine());
    }
    indentLevel--;
    b.append(indent(indentLevel)).append("</values>").append(newLine());
    b.append(indent(indentLevel)).append("<basechoices />").append(newLine());
    b.append(indent(indentLevel)).append("<invalidValues />").append(newLine());
    indentLevel--;
    b.append(indent(indentLevel))
        .append("</Parameter>\n").append(newLine());
    return indentLevel;
  }

  private static void renderConstraints(StringBuilder b, int indentLevel) {
  }

  private static String indent(int indentLevel) {
    return String.format("%" + (indentLevel * 2) + "s", "");
  }

  private static String newLine() {
    return String.format("%n");
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
}
