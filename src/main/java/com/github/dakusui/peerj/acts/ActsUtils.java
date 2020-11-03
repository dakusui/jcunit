package com.github.dakusui.peerj.acts;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import com.github.dakusui.peerj.model.NormalizedConstraint;
import com.github.dakusui.peerj.utils.CoveringArrayGenerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.utils.ConstraintUtils.*;
import static com.github.dakusui.peerj.utils.ProcessStreamerUtils.streamFile;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

public enum ActsUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(ActsUtils.class);

  public static String buildActsModel(FactorSpace factorSpace, String systemName, List<Tuple> testCases) {
    StringBuilder b = new StringBuilder();
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    b.append("<System name=\"").append(systemName).append("\">\n");
    FactorSpaceAdapter factorSpaceAdapter = new FactorSpaceAdapter(factorSpace);
    renderParameters(b, 1, factorSpaceAdapter);
    renderRelations(b, 1, factorSpaceAdapter, factorSpace.baseStrength(), factorSpace.relationStrength());
    renderConstraints(b, 1, factorSpaceAdapter, factorSpace.getConstraints());
    renderTestSet(b, 1, factorSpaceAdapter, factorSpace.baseStrength(), testCases);
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
          indexOfFactorName.apply(factorName) >= 0
              ? name.apply(indexOfFactorName.apply(factorName))
              : factorName;
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
   * @param b           A string builder with which a given parameter is rendered.
   * @param indentLevel A current indentation level.
   * @param parameterId An identifier of a given parameter as {@code parameter}.
   * @param parameter   A parameter to be rendered.
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
      if (!(each instanceof NormalizedConstraint))
        throw new UnsupportedOperationException();
      StringUtils.appendLine(b, indentLevel,
          format("<Constraint text=\"%s\">",
              ((NormalizedConstraint) each).toText(factorSpaceAdapter.factorNameToParameterName)));
      StringUtils.appendLine(b, indentLevel, "<Parameters>");
      indentLevel++;
      for (String eachFactorName : each.involvedKeys())
        StringUtils.appendLine(b,
            indentLevel,
            format("<Parameter name=\"%s\"/>", factorSpaceAdapter.factorNameToParameterName.apply(eachFactorName)));
      indentLevel--;
      StringUtils.appendLine(b, indentLevel, "</Parameters>");
      StringUtils.appendLine(b, indentLevel, "</Constraint>");
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Constraints>");
  }

  /**
   * <pre>
   *     <Relations>
   *     <Relation Strength="3" Default="false">
   *       <Parameter name="p0">
   *         <value>0</value>
   *         <value>1</value>
   *       </Parameter>
   *       <Parameter name="p1">
   *         <value>0</value>
   *         <value>1</value>
   *       </Parameter>
   *       <Parameter name="p2">
   *         <value>0</value>
   *         <value>1</value>
   *       </Parameter>
   *       ...
   *       <Parameter name="p51">
   *         <value>0</value>
   *         <value>1</value>
   *       </Parameter>
   *     </Relation>
   * </pre>
   */
  private static void renderRelations(StringBuilder b, @SuppressWarnings("SameParameterValue") int indentLevel, FactorSpaceAdapter factorSpaceAdapter, int strength, int relationStrength) {
    if (relationStrength < 0)
      return;
    StringUtils.appendLine(b, indentLevel, "<Relations>");
    indentLevel++;
    indentLevel = renderRelation(b, indentLevel, factorSpaceAdapter, 0, factorSpaceAdapter.numParameters, strength);
    indentLevel = renderRelation(b, indentLevel, factorSpaceAdapter, 0, factorSpaceAdapter.numParameters / 2, relationStrength);
    indentLevel = renderRelation(b, indentLevel, factorSpaceAdapter, factorSpaceAdapter.numParameters / 2, factorSpaceAdapter.numParameters, relationStrength);
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Relations>");
  }

  /**
   * <pre>
   *   <Testset doi="2">
   *     <Testcase TCNo="0">
   *       <Value>1</Value>
   *       <Value>0</Value>
   *       <Value>0</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *       <Value>1</Value>
   *     </Testcase>
   *   </Testset>
   * </pre>
   */
  private static void renderTestSet(StringBuilder b, @SuppressWarnings("SameParameterValue") int indentLevel, FactorSpaceAdapter factorSpaceAdapter, int strength, List<Tuple> testCases) {
    if (testCases.isEmpty())
      return;
    StringUtils.appendLine(b, indentLevel, format("<Testset doi=\"%s\">", strength));
    IntStream.range(0, testCases.size())
        .forEach(i -> renderTestcase(b, indentLevel + 1, factorSpaceAdapter, testCases.get(i), i));
    StringUtils.appendLine(b, indentLevel, "</Testset>");
  }

  private static void renderTestcase(StringBuilder b, int indentLevel, FactorSpaceAdapter factorSpaceAdapter, Tuple testCase, int testCaseNo) {
    StringUtils.appendLine(b, indentLevel, format("<Testcase TCNo=\"%s\">", testCaseNo));
    IntStream.range(0, factorSpaceAdapter.numParameters)
        .mapToObj(i -> factorSpaceAdapter.factorNameToParameterName.apply(factorSpaceAdapter.factor.apply(i).getName()))
        .map(k -> format("<Value>%s</Value>", testCase.get(k)))
        .forEach(testCaseElement -> StringUtils.appendLine(b, indentLevel + 1, testCaseElement));
    StringUtils.appendLine(b, indentLevel, "</Testcase>");
  }

  private static int renderRelation(StringBuilder b, int indentLevel, FactorSpaceAdapter factorSpaceAdapter, int begin, int end, int relationStrength) {
    StringUtils.appendLine(b, indentLevel, format("<Relation Strength=\"%s\" Default=\"false\">", relationStrength));
    indentLevel++;
    for (int i = begin; i < end; i++) {
      indentLevel = renderParameterInRelation(b, indentLevel, i, factorSpaceAdapter);
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Relation>");
    return indentLevel;
  }

  private static int renderParameterInRelation(StringBuilder b, int indentLevel, int parameterId, FactorSpaceAdapter factorSpaceAdapter) {
    b.append(StringUtils.indent(indentLevel))
        .append("<Parameter name=\"").append(factorSpaceAdapter.name.apply(parameterId)).append("\">")
        .append(StringUtils.newLine());
    indentLevel++;
    StringUtils.appendLine(b, indentLevel, "<values>");
    Factor factor = factorSpaceAdapter.factor.apply(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(StringUtils.indent(indentLevel)).append("<value>").append(factorSpaceAdapter.value.apply(parameterId).apply(j)).append("</value>").append(StringUtils.newLine());
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</values>");
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Parameter>\n");
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
   * <pre>
   *   p i,1 > p i,2 ∨ p i,3 > p i,4 ∨ p i,5 > p i,6 ∨ p i,7 > p i,8 ∨ p i,9 > p i,2
   * </pre>
   *
   * @param factorNames A list of factor names.
   */
  public static NormalizedConstraint createBasicConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return or(
        ge(p[0], p[1]),
        gt(p[2], p[3]),
        eq(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1]));
  }

  /*
      (pi,1>pi,2 ∨ pi,3>pi,4 ∨ pi,5>pi,6 ∨ pi,7>pi,8 ∨ pi,9>pi,2)
                    ∧pi,10>pi,1
                    ∧pi,9>pi,2
                    ∧pi,8>pi,3
                    ∧pi,7>pi,4
                    ∧pi,6>pi,5 (0≤i<n)
   */
  public static NormalizedConstraint createBasicPlusConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return and(or(
        ge(p[0], p[1]),
        gt(p[2], p[3]),
        eq(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1])), gt(p[9], p[0]), gt(p[8], p[1]), gt(p[7], p[2]), gt(p[6], p[3]), gt(p[5], p[4]));
  }

  public static Function<List<String>, NormalizedConstraint> createBasicConstraint(int offset) {
    return strings -> createBasicConstraint(strings.subList(offset, offset + 10));
  }

  public static Function<List<String>, NormalizedConstraint> createBasicPlusConstraint(int offset) {
    return strings -> createBasicPlusConstraint(strings.subList(offset, offset + 10));
  }
}
