package com.github.dakusui.jcunit.report;

import com.github.dakusui.jcunit.generators.TestArrayGenerator;
import com.github.dakusui.jcunit.report.Reporter.Domain;
import com.github.dakusui.lisj.Basic;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.runner.Description;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NormalReportFormatter implements ReportFormatter {
	private char domainKeycode;

	public NormalReportFormatter() {
	}

	@Override
	public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz) {
		writeLine(writer, klazz, "* MATRIX *");
		this.domainKeycode = 'A';
	}

	@Override
	public void beginDomainsSection(ReportWriter writer, Class<?> klazz) {
		writeLine(writer, klazz, "* DOMAINS *");
		this.domainKeycode = 'A';
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
	}

	@Override
	public void endDomainsSection(ReportWriter writer, Class<?> klazz) {
	}

	@Override
	public void endTestClassFooter(ReportWriter writer, Class<?> klazz) {
	}

	@Override
	public void endTestClassHeader(ReportWriter writer, Class<?> klazz) {
	}

	@Override
	public void formatConditionMatrix(ReportWriter writer, Class<?> klazz, TestArrayGenerator<Field, Object> testArrayGenerator) {
		writer.writeLine(klazz, 0, "* MATRIX *");
		////
		// print out the header
		String header = String.format("%22s", "");
		int numKeys = testArrayGenerator.getKeys().size();
		boolean firstTime = true;
		char keyCode = 'A';
		for (int i = 0; i < numKeys; i++) {
			if (firstTime)
				firstTime = false;
			else
				header += ",";
			header += String.format("%-2s", keyCode);
			keyCode++;
		}
		writer.writeLine(klazz, 0, header);
		////
		// print out matrix body.
		long size = testArrayGenerator.size();
		for (int i = 0; i < size; i++) {
			String line = String.format("%-20s", String.format("testrun[%d]:", i));
			firstTime = true;
			for (Field key : testArrayGenerator.getKeys()) {
				int valueCode = testArrayGenerator.getIndex(key, i);
				if (firstTime)
					firstTime = false;
				else
					line += ",";
				line += String.format("%02d", valueCode);
			}
			writer.writeLine(klazz, 1, line);
		}
		writer.writeLine(klazz, 0, "");
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
	public void formatValues(ReportWriter writer, String category, Description desc, Map<Field, Object> values) {
		writer.writeLine(desc, 1, String.format("VALUES(%d)", values.size()));
		List<Field> keys = new ArrayList<Field>(values.keySet());
		Collections.sort(keys, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Field key : keys) {
			Object v = values.get(key);
			writer.writeLine(
					desc,
					2,
					String.format(
							"%s:%s(%s)",
							key.getName(),
							v == null ? null : ArrayUtils.toString(v),
							key.getType().getName())
					);
		}
	}

	protected void dumpValues(Description desc, ReportWriter writer, Map<Field, Object> values) {
		writer.writeLine(desc, 1, String.format("VALUES(%d)", values.size()));
		List<Field> keys = new ArrayList<Field>(values.keySet());
		Collections.sort(keys, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Field key : keys) {
			Object v = values.get(key);
			writer.writeLine(
					desc,
					2,
					String.format(
							"%s:%s(%s)",
							key.getName(),
							v == null ? null : ArrayUtils.toString(v),
							key.getType().getName())
					);
		}
	}

	@Override
	public void formatDomain(ReportWriter writer, Class<?> klazz, Domain domain) {
		////
		// print out header
		Field key = domain.field();
		String domainHeader = String.format("%s:%s(%s)", this.domainKeycode, key.getName(), key.getType());
		writer.writeLine(klazz, 1, domainHeader);
		Object[] d = domain.values();
		for (int i = 0; i < d.length; i++) {
			String l = String.format("%02d:'%s'", i, Basic.tostr(d[i]));
			writer.writeLine(klazz, 2, l);
		}
		this.domainKeycode++;
	}

	private void writeLine(ReportWriter writer, Class<?> klazz, String s, Object... params) {
		writer.writeLine(klazz, 0, String.format(s.replaceAll("\\{\\}", "%s"), params));
	}
}
