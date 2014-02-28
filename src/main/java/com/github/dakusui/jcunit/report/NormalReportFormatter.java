package com.github.dakusui.jcunit.report;


public class NormalReportFormatter implements ReportFormatter {
	public NormalReportFormatter() {
	}

	@Override
	public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beginDomainsSection(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void beginTestClassFooter(ReportWriter writer, Class<?> klazz) {
		writeLine(writer, klazz, "***********************************************");
		writeLine(writer, klazz, "***                                         ***");
		writeLine(writer, klazz, "***          T E S T S U M M A R Y          ***");
		writeLine(writer, klazz, "***                                         ***");
		writeLine(writer, klazz, "***********************************************");
		writeLine(writer, klazz, "");
	}

	@Override
	public void beginTestClassHeader(ReportWriter writer, Class<?> klazz) {
		writeLine(writer, klazz, "***********************************************");
		writeLine(writer, klazz, "***                                         ***");
		writeLine(writer, klazz, "***          T E S T   M A T R I X          ***");
		writeLine(writer, klazz, "***                                         ***");
		writeLine(writer, klazz, "***********************************************");
		writeLine(writer, klazz, "");
	}

	@Override
	public void endConditionMatrixSection(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endDomainsSection(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endTestClassFooter(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endTestClassHeader(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatResult(ReportWriter writer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatResultMatrix(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatRules(ReportWriter writer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatRulesResult(ReportWriter writer, Class<?> klazz) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void formatValues(ReportWriter writer, String category) {
		// TODO Auto-generated method stub
		
	}

	private void writeLine(ReportWriter writer, Class<?> klazz, String s, Object... params) {
		writer.writeLine(klazz, 0, String.format(s.replaceAll("\\{\\}", "%s"), params));
	}

}
