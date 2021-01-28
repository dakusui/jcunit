package com.github.dakusui.peerj.ext.acts;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.shared.FactorSpaceTranslator;
import com.github.dakusui.peerj.ext.shared.IoUtils;
import com.github.dakusui.peerj.model.FormalizableConstraint;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;

public enum ActsUtils {
  ;

  public static List<Tuple> readTestSuiteFromCsv(Stream<String> data) {
    return IoUtils.readTestSuiteFromXsv(data, ",");
  }

  public static String buildActsModel(String systemName, FactorSpace factorSpace, List<Tuple> testCases) {
    StringBuilder b = new StringBuilder();
    b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    b.append("<System name=\"").append(systemName).append("\">\n");
    FactorSpaceTranslator factorSpaceTranslator = new FactorSpaceTranslator(factorSpace);
    renderParameters(b, 1, factorSpaceTranslator);
    renderRelations(b, 1, factorSpaceTranslator, factorSpace.baseStrength(), factorSpace.relationStrength());
    renderConstraints(b, 1, factorSpaceTranslator, factorSpace.getConstraints());
    renderTestSet(b, 1, factorSpaceTranslator, factorSpace.baseStrength(), testCases);
    b.append("\n");
    b.append("</System>");

    return b.toString();
  }

  @SuppressWarnings("SameParameterValue")
  private static void renderParameters(StringBuilder b, int indentLevel, FactorSpaceTranslator factorSpaceTranslator) {
    StringUtils.appendLine(b, indentLevel, "<Parameters>");
    indentLevel++;
    for (int i = 0; i < factorSpaceTranslator.numFactors(); i++) {
      indentLevel = renderParameter(
          b,
          indentLevel,
          i,
          factorSpaceTranslator);
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
   * @param b                     A string builder with which a given factorSpaceTranslator is rendered.
   * @param indentLevel           A current indentation level.
   * @param parameterId           An identifier of a given factorSpaceTranslator as {@code factorSpaceTranslator}.
   * @param factorSpaceTranslator A factorSpaceTranslator to be rendered.
   * @return The indentation level after the given factorSpaceTranslator is rendered.
   */
  private static int renderParameter(StringBuilder b, int indentLevel, int parameterId, FactorSpaceTranslator factorSpaceTranslator) {
    b.append(StringUtils.indent(indentLevel))
        .append("<Parameter id=\"").append(parameterId).append("\" name=\"")
        .append(factorSpaceTranslator.formalFactorNameOf(parameterId))
        .append("\" type=\"")
        .append("0")
        .append("\">")
        .append(StringUtils.newLine());
    indentLevel++;
    StringUtils.appendLine(b, indentLevel, "<values>");
    Factor factor = factorSpaceTranslator.factorFor(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(StringUtils.indent(indentLevel)).append("<value>").append(factorSpaceTranslator.formalFactorLevelOf(parameterId, j)).append("</value>").append(StringUtils.newLine());
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
  private static void renderConstraints(StringBuilder b, int indentLevel, FactorSpaceTranslator factorSpaceTranslator, List<Constraint> constraints) {
    if (constraints.isEmpty())
      return;
    StringUtils.appendLine(b, indentLevel, "<Constraints>");
    indentLevel++;
    for (Constraint each : constraints) {
      if (!(each instanceof FormalizableConstraint))
        throw new UnsupportedOperationException();
      StringUtils.appendLine(b, indentLevel,
          format("<Constraint text=\"%s\">",
              ((FormalizableConstraint) each).toText(term -> factorSpaceTranslator.formalizeFactorName(term).orElse(term))));
      StringUtils.appendLine(b, indentLevel, "<Parameters>");
      indentLevel++;
      for (String eachFactorName : each.involvedKeys())
        StringUtils.appendLine(b,
            indentLevel,
            format("<Parameter name=\"%s\"/>",
                factorSpaceTranslator.formalizeFactorName(eachFactorName)
                    .orElseThrow(NoSuchElementException::new)));
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
  private static void renderRelations(StringBuilder b, @SuppressWarnings("SameParameterValue") int indentLevel, FactorSpaceTranslator factorSpaceTranslator, int strength, int relationStrength) {
    if (relationStrength < 0)
      return;
    StringUtils.appendLine(b, indentLevel, "<Relations>");
    indentLevel++;
    indentLevel = renderRelation(b, indentLevel, factorSpaceTranslator, 0, factorSpaceTranslator.numFactors(), strength);
    indentLevel = renderRelation(b, indentLevel, factorSpaceTranslator, 0, factorSpaceTranslator.numFactors() / 2, relationStrength);
    indentLevel = renderRelation(b, indentLevel, factorSpaceTranslator, factorSpaceTranslator.numFactors() / 2, factorSpaceTranslator.numFactors(), relationStrength);
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
  private static void renderTestSet(StringBuilder b, @SuppressWarnings("SameParameterValue") int indentLevel, FactorSpaceTranslator factorSpaceTranslator, int strength, List<Tuple> testCases) {
    if (testCases.isEmpty())
      return;
    StringUtils.appendLine(b, indentLevel, format("<Testset doi=\"%s\">", strength));
    IntStream.range(0, testCases.size())
        .forEach(i -> renderTestcase(b, indentLevel + 1, factorSpaceTranslator, testCases.get(i), i));
    StringUtils.appendLine(b, indentLevel, "</Testset>");
  }

  private static void renderTestcase(StringBuilder b, int indentLevel, FactorSpaceTranslator factorSpaceTranslator, Tuple testCase, int testCaseNo) {
    StringUtils.appendLine(b, indentLevel, format("<Testcase TCNo=\"%s\">", testCaseNo));
    IntStream.range(0, factorSpaceTranslator.numFactors())
        .mapToObj(factorSpaceTranslator::formalFactorNameOf)
        .map(k -> format("<Value>%s</Value>", testCase.get(k)))
        .forEach(testCaseElement -> StringUtils.appendLine(b, indentLevel + 1, testCaseElement));
    StringUtils.appendLine(b, indentLevel, "</Testcase>");
  }

  private static int renderRelation(StringBuilder b, int indentLevel, FactorSpaceTranslator factorSpaceTranslator, int begin, int end, int relationStrength) {
    StringUtils.appendLine(b, indentLevel, format("<Relation Strength=\"%s\" Default=\"false\">", relationStrength));
    indentLevel++;
    for (int i = begin; i < end; i++) {
      indentLevel = renderParameterInRelation(b, indentLevel, i, factorSpaceTranslator);
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Relation>");
    return indentLevel;
  }


  public static int renderParameterInRelation(StringBuilder b, int indentLevel, int parameterId, FactorSpaceTranslator factorSpaceTranslator) {
    b.append(StringUtils.indent(indentLevel))
        .append("<Parameter name=\"").append(factorSpaceTranslator.formalFactorNameOf(parameterId)).append("\">")
        .append(StringUtils.newLine());
    indentLevel++;
    StringUtils.appendLine(b, indentLevel, "<values>");
    Factor factor = factorSpaceTranslator.factorFor(parameterId);
    indentLevel++;
    for (int j = 0; j < factor.getLevels().size(); j++) {
      b.append(StringUtils.indent(indentLevel)).append("<value>").append(factorSpaceTranslator.formalFactorLevelOf(parameterId, j)).append("</value>").append(StringUtils.newLine());
    }
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</values>");
    indentLevel--;
    StringUtils.appendLine(b, indentLevel, "</Parameter>\n");
    return indentLevel;
  }
}
