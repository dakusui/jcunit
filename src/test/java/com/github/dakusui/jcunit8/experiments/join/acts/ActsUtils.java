package com.github.dakusui.jcunit8.experiments.join.acts;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.factorspace.Constraint;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum ActsUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(ActsUtils.class);

  static String buildActsModel(FactorSpace factorSpace, String systemName) {
    StringBuilder b = new StringBuilder();
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    b.append("<System name=\"").append(systemName).append("\">\n");
    FactorSpaceAdapter factorSpaceAdapter = new FactorSpaceAdapter(factorSpace);
    renderParameters(b, 1, factorSpaceAdapter);
    renderConstraints(b, 1, factorSpaceAdapter, factorSpace.getConstraints());
    b.append("\n");
    b.append("</System>");

    return b.toString();
  }

  private static class FactorSpaceAdapter {
    static final Function<Integer, String> NAME_RESOLVER =
        (id) -> String.format("p%d", id);
    final Function<Integer, String> name;
    final Function<Integer, String> type;
    final Function<Integer, Factor> factor;
    final Function<Integer, Function<Integer, Object>> value;
    final int numParameters;
    final Function<String, String> factorNameToParameterName;

    private FactorSpaceAdapter(
        Function<Integer, String> name,
        Function<Integer, String> type,
        Function<Integer, Factor> factor,
        Function<Integer, Function<Integer, Object>> value,
        Function<String, Integer> indexOfFactorName,
        int numParameters) {
      this.name = name;
      this.type = type;
      this.factor = factor;
      this.value = value;
      this.factorNameToParameterName = factorName ->
          name.apply(indexOfFactorName.apply(factorName));
      this.numParameters = numParameters;
    }

    FactorSpaceAdapter(FactorSpace factorSpace) {
      this(NAME_RESOLVER,
          (id) -> "0",
          (id) -> factorSpace.getFactors().get(id),
          (ii) -> (j) -> factorSpace.getFactors().get(ii).getLevels().get(j),
          (factorName) -> factorSpace.getFactorNames().indexOf(factorName),
          factorSpace.getFactors().size());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private static void renderParameters(StringBuilder b, int indentLevel, FactorSpaceAdapter factorSpaceAdapter) {
    appendLine(b, indentLevel, "<Parameters>");
    indentLevel++;
    for (int i = 0; i < factorSpaceAdapter.numParameters; i++) {
      indentLevel = renderParameter(
          b,
          indentLevel,
          i,
          factorSpaceAdapter);
    }
    appendLine(b, indentLevel, "</Parameters>");
  }

  private static int renderParameter(StringBuilder b, int indentLevel, int parameterId, FactorSpaceAdapter parameter) {
    b.append(indent(indentLevel))
        .append("<Parameter id=\"").append(parameterId).append("\" name=\"")
        .append(parameter.name.apply(parameterId))
        .append("\" type=\"")
        .append(parameter.type.apply(parameterId))
        .append("\">")
        .append(newLine());
    indentLevel++;
    appendLine(b, indentLevel, "<values>");
    Factor factor = parameter.factor.apply(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(indent(indentLevel)).append("<value>").append(parameter.value.apply(parameterId).apply(j)).append("</value>").append(newLine());
    }
    indentLevel--;
    appendLine(b, indentLevel, "</values>");
    appendLine(b, indentLevel, "<basechoices />");
    appendLine(b, indentLevel, "<invalidValues />");
    indentLevel--;
    appendLine(b, indentLevel, "</Parameter>\n");
    return indentLevel;
  }

  /**
   * <pre>
   *     <Constraints>
   *       <Constraint text="l01 &lt;= l02 || l03 &lt;= l04 || l05 &lt;= l06 || l07&lt;= l08 || l09 &lt;= l02">
   *       <Parameters>
   *         <Parameter name="l01" />
   *         <Parameter name="l02" />
   *         <Parameter name="l03" />
   *         <Parameter name="l04" />
   *         <Parameter name="l05" />
   *         <Parameter name="l06" />
   *         <Parameter name="l07" />
   *         <Parameter name="l08" />
   *         <Parameter name="l09" />
   *         <Parameter name="l02" />
   *       </Parameters>
   *     </Constraint>
   *   </Constraints>
   * </pre>
   */
  @SuppressWarnings("SameParameterValue")
  private static void renderConstraints(StringBuilder b, int indentLevel, FactorSpaceAdapter factorSpaceAdapter, List<Constraint> constraints) {
    if (constraints.isEmpty())
      return;
    appendLine(b, indentLevel, "<Constraints>");
    indentLevel++;
    for (Constraint each : constraints) {
      if (!(each instanceof ActsPredicate))
        throw new UnsupportedOperationException();
      appendLine(b, indentLevel,
          format("<Constraint text=\"%s\">",
              ((ActsPredicate) each).toText(factorSpaceAdapter.factorNameToParameterName)));
      appendLine(b, indentLevel, "<Parameters>");
      indentLevel++;
      for (String eachFactorName : each.involvedKeys())
        appendLine(b,
            indentLevel,
            format("<Parameter name=\"%s\"/>",
                factorSpaceAdapter.factorNameToParameterName.apply(eachFactorName)));
      indentLevel--;
      appendLine(b, indentLevel, "</Parameters>");
      appendLine(b, indentLevel, "</Constraint>");
    }
    indentLevel--;
    appendLine(b, indentLevel, "</Constraints>");
  }

  /**
   * <pre>
   *     <Constraints>
   *       <Constraint text="l01 &lt;= l02 || l03 &lt;= l04 || l05 &lt;= l06 || l07&lt;= l08 || l09 &lt;= l02">
   *       <Parameters>
   *         <Parameter name="l01" />
   *         <Parameter name="l02" />
   *         <Parameter name="l03" />
   *         <Parameter name="l04" />
   *         <Parameter name="l05" />
   *         <Parameter name="l06" />
   *         <Parameter name="l07" />
   *         <Parameter name="l08" />
   *         <Parameter name="l09" />
   *         <Parameter name="l02" />
   *       </Parameters>
   *     </Constraint>
   *   </Constraints>
   * </pre>
   * <pre>
   *   p i,1 > p i,2 ∨ p i,3 > p i,4 ∨ p i,5 > p i,6 ∨ p i,7 > p i,8 ∨ p i,9 > p i,2
   * </pre>
   *
   * @param factorNames A list of factor names.
   */
  public static ActsConstraint createConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return or(
        gt(p[0], p[1]),
        gt(p[2], p[3]),
        gt(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1]));
  }

  public static Function<List<String>, ActsConstraint> createConstraint(int offset) {
    return strings -> createConstraint(strings.subList(offset, offset + 10));
  }

  private static ActsConstraint or(ActsConstraint... constraints) {
    return new ActsConstraint() {
      @Override
      public String toText(Function<String, String> factorNameToParameterName) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(factorNameToParameterName))
            .collect(Collectors.joining(" || "));
      }

      @Override
      public boolean test(Tuple tuple) {
        for (ActsConstraint each : constraints) {
          if (each.test(tuple))
            return true;
        }
        return false;
      }

      @Override
      public List<String> involvedKeys() {
        return Arrays.stream(constraints)
            .flatMap(each -> each.involvedKeys().stream())
            .distinct()
            .collect(Collectors.toList());
      }
    };
  }

  private static ActsConstraint gt(String f, String g) {
    return new ActsConstraint() {
      @Override
      public String toText(Function<String, String> factorNameToParameterName) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameToParameterName.apply(g) + " &lt; " + factorNameToParameterName.apply(f);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean test(Tuple tuple) {
        Checks.checkcond(tuple.get(f) instanceof Comparable);
        Checks.checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) > 0;
      }

      @Override
      public List<String> involvedKeys() {
        return asList(f, g);
      }
    };
  }

  private static void appendLine(StringBuilder b, int indentLevel, String s) {
    b.append(indent(indentLevel)).append(s).append(newLine());
  }


  private static String indent(int indentLevel) {
    return format("%" + (indentLevel * 2) + "s", "");
  }

  private static String newLine() {
    return format("%n");
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
          List<String> foundErrors = new LinkedList<>();

          @Override
          public boolean getAsBoolean() {
            return foundErrors.isEmpty();
          }

          @Override
          public void accept(String s) {
            if (s.contains("Errors encountered"))
              foundErrors.add(s);
            if (s.contains("Constraints can not be parsed"))
              foundErrors.add(s);
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
    return processStreamer(format("cat %s", file.getAbsolutePath()), ProcessStreamer.Checker.createDefault()).stream();
  }

  private static ProcessStreamer processStreamer(String command, ProcessStreamer.Checker checker) {
    LOGGER.debug("Executing:[{}]", command);
    return new ProcessStreamer.Builder(Shell.local(), command)
        .checker(checker)
        .build();
  }

  private static void writeTo(File file, String data) {
    processStreamer(format("echo '%s' > %s", data, file.getAbsolutePath()), ProcessStreamer.Checker.createDefault())
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

  @SafeVarargs
  public static void generateAndReport(File baseDir, int numLevels, int numFactors, int strength, Function<List<String>, ActsConstraint>... constraints) {
    CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
    List<Tuple> generated;
    generated = generateWithActs(baseDir, numLevels, numFactors, strength, constraints);
    System.out.println("size=" + generated.size() + " time=" + stopWatch.get() + "[msec]");
  }

  @SafeVarargs
  public static List<Tuple> generateWithActs(File baseDir, int numLevels, int numFactors, int strength, Function<List<String>, ActsConstraint>... constraints) {
    FactorSpaceSpec factorSpaceSpec = new FactorSpaceSpec("L").addFactors(numLevels, numFactors);
    for (Function<List<String>, ActsConstraint> each : constraints)
      factorSpaceSpec = factorSpaceSpec.addConstraint(each);
    FactorSpace factorSpace = factorSpaceSpec.build();
    generateWithActs(
        baseDir,
        factorSpace,
        strength);
    List<Tuple> ret = new LinkedList<>();
    try (Stream<String> data = streamFile(outFile(baseDir))) {
      ret.addAll(readTestSuiteFromCsv(data));
    }
    return ret;
  }

}
