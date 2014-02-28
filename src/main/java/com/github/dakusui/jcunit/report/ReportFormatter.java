package com.github.dakusui.jcunit.report;



public interface ReportFormatter {
	public void beginTestClassHeader(ReportWriter writer, Class<?> klazz);
	public void endTestClassHeader(ReportWriter writer, Class<?> klazz);
	
	public void beginDomainsSection(ReportWriter writer, Class<?> klazz);
	public void endDomainsSection(ReportWriter writer, Class<?> klazz);
	
	public void beginTestClassFooter(ReportWriter writer, Class<?> klazz);
	public void endTestClassFooter(ReportWriter writer, Class<?> klazz);

	public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz);
	public void endConditionMatrixSection(ReportWriter writer, Class<?> klazz);
		
	public void formatValues(ReportWriter writer, String category);
	
	public void formatRules(ReportWriter writer);
	
	public void formatResult(ReportWriter writer);

	public void formatResultMatrix(ReportWriter writer, Class<?> klazz);
	
	public void formatRulesResult(ReportWriter writer, Class<?> klazz);
}
