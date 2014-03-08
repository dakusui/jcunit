package com.github.dakusui.jcunit.report;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.Description;

import com.github.dakusui.jcunit.core.BasicSummarizer.ResultMatrix;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;

public abstract class Reporter {
  private ReportWriter    writer;
  private ReportFormatter formatter;

  public Reporter(ReportWriter writer, ReportFormatter formatter) {
    this.writer = writer;
    this.formatter = formatter;
  }

  public void initTestClassLevelReport(Class<?> targetClass) {
    writer.deleteReport(targetClass);
  }

  public void initTestCaseLevelReport(Description desc) {
    writer.deleteReport(desc);
  }

  public void writeTestClassHeader(Class<?> testClass, TestArrayGenerator<Field, Object> testArrayGenerator) {
    this.formatter.beginTestClassHeader(writer, testClass);
    try {
      this.formatter.beginDomainsSection(writer, testClass);
      try {
        for (Domain domain : domains(testArrayGenerator)) {
          this.formatter.formatDomain(writer, testClass, domain);
        }
      } finally {
        this.formatter.endDomainsSection(writer, testClass);
      }
      this.formatter.beginConditionMatrixSection(writer, testClass);
      try {
        this.formatter.formatConditionMatrix(writer, testClass, testArrayGenerator);
      } finally {
        this.formatter.endConditionMatrixSection(writer, testClass);
      }
    } finally {
      this.formatter.endTestClassHeader(writer, testClass);
    }
  }

  public void writeTestCaseReport(Description desc, boolean ok, List<FieldSet> fieldSets) {
    this.formatter.beginTestCase(writer, desc);
    try {
      this.formatter.formatResult(writer, desc, ok);
      this.formatter.formatRuleSet(writer, desc, null); // TODO
      for (FieldSet fields : fieldSets) {
        this.formatter.formatValues(writer, desc, fields);
      }
    } finally {
      this.formatter.endTestCase(writer, desc);
    }
  }

  public void writeTestClassFooter(Class<?> testClass, RuleSet ruleSet, ResultMatrix matrix) {
    this.formatter.beginTestClassFooter(writer, testClass);
    try {
      this.formatter.formatResultMatrix(writer, testClass, ruleSet, matrix);
      this.formatter.formatRulesResult(writer, testClass);
    } finally {
      this.formatter.endTestClassFooter(writer, testClass);
    }
  }

  private List<Domain> domains(TestArrayGenerator<Field, Object> testArrayGenerator) {
    List<Domain> ret = new LinkedList<Domain>();
    for (Field key : testArrayGenerator.getKeys()) {
      Object[] values = testArrayGenerator.getDomain(key);
      ret.add(new Domain(key, values));
    }
    return ret;
  }

  public static class Domain {
    private Field    field;
    private Object[] values;

    Domain(Field field, Object[] values) {
      this.field = field;
      this.values = values;
    }

    public Field field() {
      return this.field;
    }

    public Object[] values() {
      return this.values;
    }
  }

  public static class FieldSet extends HashMap<Field, Object> {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 5779222683653111869L;
    /*
     * Category name to which this field set belongs.
     */
    private String            categoryName;

    /**
     * Creates an object of this class.
     * 
     * @param categoryName
     *          A category name to which this field set belongs.
     */
    public FieldSet(String categoryName) {
      this.categoryName = categoryName;
    }

    /**
     * Returns category name.
     * 
     * @return A category name to which this field set belongs.
     */
    public String categoryName() {
      return this.categoryName;
    }
  }

  public static enum TestCaseResult {
    PASS, FAIL, ABORT
  }
}
