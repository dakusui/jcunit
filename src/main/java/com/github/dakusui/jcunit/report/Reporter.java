package com.github.dakusui.jcunit.report;

import com.github.dakusui.jcunit.generators.TestArrayGenerator;

import org.junit.runner.Description;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public abstract class Reporter {
	private ReportWriter writer;
	private ReportFormatter formatter;

	public Reporter(ReportWriter writer, ReportFormatter formatter) {
		this.writer = writer;
		this.formatter = formatter;
	}

	public void initTestClassLevelReport(Class<?> targetClass) {
		writer.deleteReport(targetClass);
	}

	public void writeTestClassHeader(
			Class<?> testClass,
			TestArrayGenerator<Field, Object> testArrayGenerator) {
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

	public void writeTestCaseReport(Description desc) {
		this.formatter.formatResult(writer);
		this.formatter.formatValues(writer, "INPUT", desc, null);
		this.formatter.formatRules(writer);
		////
		// Optional.
		this.formatter.formatValues(writer, "STORED", desc, null);
		this.formatter.formatValues(writer, "OUTPUT", desc, null);
	}

	public void writeTestClassFooter(Class<?> testClass) {
		this.formatter.beginTestClassFooter(writer, testClass);
		try {
			this.formatter.formatResultMatrix(writer, testClass);
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
		private Field field;
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

	public static enum TestResult {
		PASS,
		FAIL,
		ABORT
	}
}
