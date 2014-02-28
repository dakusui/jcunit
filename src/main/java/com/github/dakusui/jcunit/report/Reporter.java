package com.github.dakusui.jcunit.report;

import org.junit.runner.Description;


public abstract class Reporter {
	private ReportWriter writer;
	private ReportFormatter formatter;

	public Reporter(ReportWriter writer, ReportFormatter formatter) {
		this.writer = writer;
		this.formatter = formatter;
	}
	
	public void writeTestClassHeader(Class<?> testClass) {
		this.formatter.beginTestClassHeader(writer, testClass);
		try {
			this.formatter.beginDomainsSection(writer, testClass);
			try {
			} finally {
				this.formatter.endConditionMatrixSection(writer, testClass);
			}
			try {
				
			} finally {
				this.formatter.endDomainsSection(writer, testClass);
			}
		} finally {
			this.formatter.endTestClassHeader(writer, testClass);
		}
	}
	
	public void writeTestCaseReport(Description desc) {
		this.formatter.formatResult(writer);
		this.formatter.formatValues(writer, "INPUT");
		this.formatter.formatRules(writer);
		////
		// Optional.
		this.formatter.formatValues(writer, "STORED");
		this.formatter.formatValues(writer, "OUTPUT");
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

}
