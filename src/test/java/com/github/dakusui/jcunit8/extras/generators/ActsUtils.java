package com.github.dakusui.jcunit8.extras.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.extras.normalizer.compat.NormalizedConstraint;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.ProcessStreamerUtils.streamFile;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public enum ActsUtils {
  ;
  private static Logger LOGGER = LoggerFactory.getLogger(ActsUtils.class);

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
    static final Function<Integer, String>                    NAME_RESOLVER =
        (id) -> String.format("p%d", id);
    final        Function<Integer, String>                    name;
    final        Function<Integer, String>                    type;
    final        Function<Integer, Factor>                    factor;
    final        Function<Integer, Function<Integer, Object>> value;
    final        int                                          numParameters;
    final        Function<String, String>                     factorNameToParameterName;

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
    StringUtils.appendLine(b, indentLevel, "<Parameters>");
    indentLevel++;
    for (int i = 0; i < factorSpaceAdapter.numParameters; i++) {
      indentLevel = renderParameter(
          b,
          indentLevel,
          i,
          factorSpaceAdapter);
    }
    StringUtils.appendLine(b, indentLevel, "</Parameters>");
  }

  /**
   * <ul>
   * <li>type:0: Number and Range</li>
   * <li>type:1: Enum </li>
   * <li>type:2: bool</li>
   * </ul>
   * <pre>
   *   <Parameters>
   *     <Parameter id="2" name="enum1" type="1">
   *       <values>
   *         <value>elem1</value>
   *         <value>elem2</value>
   *       </values>
   *       <basechoices />
   *       <invalidValues />
   *     </Parameter>
   *     <Parameter id="3" name="num1" type="0">
   *       <values>
   *         <value>0</value>
   *         <value>100</value>
   *         <value>123</value>
   *         <value>1000000</value>
   *         <value>2000000000</value>
   *         <value>-2000000000</value>
   *       </values>
   *       <basechoices />
   *       <invalidValues />
   *     </Parameter>
   *     <Parameter id="4" name="bool1" type="2">
   *       <values>
   *         <value>true</value>
   *         <value>false</value>
   *       </values>
   *       <basechoices />
   *       <invalidValues />
   *     </Parameter>
   *     <Parameter id="5" name="range1" type="0">
   *       <values>
   *         <value>0</value>
   *         <value>1</value>
   *         <value>2</value>
   *         <value>3</value>
   *       </values>
   *       <basechoices />
   *       <invalidValues />
   *     </Parameter>
   *   </Parameters>
   * </pre>
   *
   * @param b A string builder with which a given parameter is rendered.
   * @param indentLevel A current indentation level.
   * @param parameterId An identifier of a given parameter as {@code parameter}.
   * @param parameter A parameter to be rendered.
   * @return The indentation level after the given parameter is rendered.
   */
  private static int renderParameter(StringBuilder b, int indentLevel, int parameterId, FactorSpaceAdapter parameter) {
    b.append(StringUtils.indent(indentLevel))
        .append("<Parameter id=\"").append(parameterId).append("\" name=\"")
        .append(parameter.name.apply(parameterId))
        .append("\" type=\"")
        .append(parameter.type.apply(parameterId))
        .append("\">")
        .append(StringUtils.newLine());
    indentLevel++;
    StringUtils.appendLine(b, indentLevel, "<values>");
    Factor factor = parameter.factor.apply(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(StringUtils.indent(indentLevel)).append("<value>").append(parameter.value.apply(parameterId).apply(j)).append("</value>").append(StringUtils.newLine());
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</values>");
    StringUtils.appendLine(b, indentLevel, "<basechoices />");
    StringUtils.appendLine(b, indentLevel, "<invalidValues />");
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Parameter>\n");
    return indentLevel;
  }

  /**
   * <pre>
   *     <Constraints>
   *     <Constraint text="(num1 &gt;= 100 &amp;&amp; bool1) &amp;&amp; (enum1 == &quot;elem2&quot;)"><Parameters>
   *       <Parameter name="bool1"/>
   *       <Parameter name="num1"/>
   *       <Parameter name="enum1"/>
   *     </Parameters></Constraint>
   *   </Constraints>
   * </pre>
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
    StringUtils.appendLine(b, indentLevel, "<Constraints>");
    indentLevel++;
    for (Constraint each : constraints) {
      if (!(each instanceof ActsPredicate))
        throw new UnsupportedOperationException();
      StringUtils.appendLine(b, indentLevel,
          format("<Constraint text=\"%s\">",
              ((ActsPredicate) each).toText(factorSpaceAdapter.factorNameToParameterName)));
      StringUtils.appendLine(b, indentLevel, "<Parameters>");
      indentLevel++;
      for (String eachFactorName : each.involvedKeys())
        StringUtils.appendLine(b,
            indentLevel,
            format("<Parameter name=\"%s\"/>",
                factorSpaceAdapter.factorNameToParameterName.apply(eachFactorName)));
      indentLevel--;
      StringUtils.appendLine(b, indentLevel, "</Parameters>");
      StringUtils.appendLine(b, indentLevel, "</Constraint>");
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Constraints>");
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
  public static NormalizedConstraint createConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return or(
        ge(p[0], p[1]),
        gt(p[2], p[3]),
        eq(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1]));
  }

  public static Function<List<String>, NormalizedConstraint> createConstraint(int offset) {
    return strings -> createConstraint(strings.subList(offset, offset + 10));
  }

  private static NormalizedConstraint or(NormalizedConstraint... constraints) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameToParameterName) {
        return Arrays.stream(constraints)
            .map(each -> each.toText(factorNameToParameterName))
            .collect(joining(" || "));
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean test(Tuple tuple) {
        for (NormalizedConstraint each : constraints) {
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

  private static NormalizedConstraint gt(String f, String g) {
    return new Comp(f, g) {

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private static NormalizedConstraint ge(String f, String g) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameNormalizer.apply(g) + " &lt;= " + factorNameNormalizer.apply(f);
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) >= 0;
      }

      @Override
      public List<String> involvedKeys() {
        return asList(f, g);
      }
    };
  }

  private static NormalizedConstraint eq(String f, String g) {
    return new NormalizedConstraint() {
      @Override
      public String toText(Function<String, String> factorNameNormalizer) {
        ////
        // Since ACTS seems not supporting > (&gt;), invert the comparator.
        return factorNameNormalizer.apply(g) + " &lt;= " + factorNameNormalizer.apply(f);
      }

      @Override
      public String getName() {
        throw new UnsupportedOperationException();
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean test(Tuple tuple) {
        checkcond(tuple.get(f) instanceof Comparable);
        checkcond(tuple.get(g) instanceof Comparable);
        return ((Comparable) f).compareTo(g) == 0;
      }

      @Override
      public List<String> involvedKeys() {
        return asList(f, g);
      }
    };
  }


  @SafeVarargs
  public static void generateAndReport(File baseDir, int numLevels, int numFactors, int strength, Function<List<String>, NormalizedConstraint>... constraints) {
    CoveringArrayGenerationUtils.StopWatch stopWatch = new CoveringArrayGenerationUtils.StopWatch();
    List<Tuple> generated;
    generated = generateWithActs(baseDir, numLevels, numFactors, strength, constraints);
    System.out.println("model=" + numLevels + "^" + numFactors + " t=" + strength + " size=" + generated.size() + " time=" + stopWatch.get() + "[msec]");
  }

  @SafeVarargs
  public static List<Tuple> generateWithActs(File baseDir, int numLevels, int numFactors, int strength, Function<List<String>, NormalizedConstraint>... constraints) {
    FactorSpaceSpecForExperiments factorSpaceSpec = new CompatFactorSpaceSpecForExperiments("L").addFactors(numLevels, numFactors);
    for (Function<List<String>, NormalizedConstraint> each : constraints)
      factorSpaceSpec = factorSpaceSpec.addConstraint(each);
    FactorSpace factorSpace = factorSpaceSpec.build();
    Acts.generateWithActs(
        baseDir,
        factorSpace,
        strength);
    List<Tuple> ret = new LinkedList<>();
    try (Stream<String> data = streamFile(Acts.outFile(baseDir)).peek(LOGGER::trace)) {
      ret.addAll(Acts.readTestSuiteFromCsv(data));
    }
    return ret;
  }

  abstract static class Comp implements NormalizedConstraint {
    private final String g;
    private final String f;

    public Comp(String f, String g) {
      this.g = g;
      this.f = f;
    }

    @Override
    public String toText(Function<String, String> factorNameNormalizer) {
      ////
      // Since ACTS seems not supporting > (&gt;), invert the comparator.
      return toText(factorNameNormalizer.apply(g), factorNameNormalizer.apply(f));
    }

    public String toText(String normalizedFactorNameForG, String normalizedFactorNameForF) {
      return normalizedFactorNameForG + " &lt; " + normalizedFactorNameForF;
    }

    @Override
    public boolean test(Tuple tuple) {
      checkcond(tuple.get(f) instanceof Comparable);
      checkcond(tuple.get(g) instanceof Comparable);
      return compare(f, g);
    }

    public static boolean compare(Comparable f, Comparable g) {
      return f.compareTo(g) > 0;
    }

    @Override
    public List<String> involvedKeys() {
      return asList(f, g);
    }
  }
}
