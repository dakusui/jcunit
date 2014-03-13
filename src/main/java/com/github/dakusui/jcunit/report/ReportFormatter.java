package com.github.dakusui.jcunit.report;

import java.lang.reflect.Field;

import org.junit.runner.Description;

import com.github.dakusui.jcunit.core.BasicSummarizer.ResultMatrix;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;
import com.github.dakusui.jcunit.report.Reporter.Domain;
import com.github.dakusui.jcunit.report.Reporter.FieldSet;

public interface ReportFormatter {
  public void beginTestCase(ReportWriter writer, Description desc);

  public void endTestCase(ReportWriter writer, Description desc);

  public void beginTestClassHeader(ReportWriter writer, Class<?> klazz);

  public void endTestClassHeader(ReportWriter writer, Class<?> klazz);

  public void beginDomainsSection(ReportWriter writer, Class<?> klazz);

  public void endDomainsSection(ReportWriter writer, Class<?> klazz);

  public void beginTestClassFooter(ReportWriter writer, Class<?> klazz);

  public void endTestClassFooter(ReportWriter writer, Class<?> klazz);

  public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz);

  public void endConditionMatrixSection(ReportWriter writer, Class<?> klazz);

  public void formatValues(ReportWriter writer, Description desc,
      FieldSet fields);

  public void formatRuleSet(ReportWriter writer, Description desc,
      RuleSet ruleSet);

  public void formatResult(ReportWriter writer, Description desc, boolean ok);

  public void formatResultMatrix(ReportWriter writer, Class<?> klazz,
      RuleSet ruleSet, ResultMatrix matrix);

  public void formatRulesResult(ReportWriter writer, Class<?> klazz);

  public void formatDomain(ReportWriter writer, Class<?> testClass,
      Domain domain);

  public void formatConditionMatrix(ReportWriter writer, Class<?> testClass,
      TestArrayGenerator<Field, Object> testArrayGenerator);

}
