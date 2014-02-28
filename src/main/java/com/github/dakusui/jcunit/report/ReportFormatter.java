package com.github.dakusui.jcunit.report;

import com.github.dakusui.jcunit.generators.TestArrayGenerator;
import com.github.dakusui.jcunit.report.Reporter.Domain;

import org.junit.runner.Description;

import java.lang.reflect.Field;
import java.util.Map;

public interface ReportFormatter {
	public void beginTestClassHeader(ReportWriter writer, Class<?> klazz);

	public void endTestClassHeader(ReportWriter writer, Class<?> klazz);

	public void beginDomainsSection(ReportWriter writer, Class<?> klazz);

	public void endDomainsSection(ReportWriter writer, Class<?> klazz);

	public void beginTestClassFooter(ReportWriter writer, Class<?> klazz);

	public void endTestClassFooter(ReportWriter writer, Class<?> klazz);

	public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz);

	public void endConditionMatrixSection(ReportWriter writer, Class<?> klazz);

	public void formatValues(ReportWriter writer, String category, Description desc, Map<Field, Object> values);

	public void formatRules(ReportWriter writer);

	public void formatResult(ReportWriter writer);

	public void formatResultMatrix(ReportWriter writer, Class<?> klazz);

	public void formatRulesResult(ReportWriter writer, Class<?> klazz);

	public void formatDomain(ReportWriter writer, Class<?> testClass, Domain domain);

	public void formatConditionMatrix(ReportWriter writer, Class<?> testClass, TestArrayGenerator<Field, Object> testArrayGenerator);
}
