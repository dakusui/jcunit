package com.github.dakusui.jcunit.core;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BasicSummarizer implements TestRule, Summarizer {
	private Set<String> errorObjects = new HashSet<String>();

	private static class Matrix {
		Map<String, Map<Integer, Boolean>> map = new LinkedHashMap<String, Map<Integer, Boolean>>();
		////
		// The order where object id's are coming in isn't sort, the client
		// side needs to sort it again either way. So, I don't use LinkedHashSet
		// here.
		Set<Integer> objIds = new HashSet<Integer>();

		public void set(String testName, int objId, boolean result) {
			add(testName);
			map.get(testName).put(objId, result);
			objIds.add(objId);
		}

		public void add(String testName) {
			if (!map.containsKey(testName)) {
				map.put(testName, new LinkedHashMap<Integer, Boolean>());
			}
		}

		public boolean hasEntry(String testName, int objId) {
			if (map.containsKey(testName))
				if (map.get(testName).containsKey(objId))
					return true;
			return false;
		}

		public boolean get(String testName, int objId) {
			if (!map.containsKey(testName))
				throw new RuntimeException();
			if (!map.get(testName).containsKey(objId))
				throw new RuntimeException();
			return map.get(testName).get(objId);
		}

		public Set<String> testNames() {
			return map.keySet();
		}

		public int count(int objId, boolean b) {
			int ret = 0;
			for (String testName : testNames()) {
				if (hasEntry(testName, objId) && (get(testName, objId) == b)) {
					ret++;
				}
			}
			return ret;
		}
	}

	static enum Result {
		OK,
		NG,
		ABT,
		ERR
	}

	Matrix matrix = new Matrix();
	private RuleSet ruleSet;
	private Map<String, Result> resultMap = new HashMap<String, Result>();

	ReportWriter writer = new ReportWriter();

	public Statement apply(Statement base, Description description) {
		return statement(base);
	}

	private Statement statement(final Statement base) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				base.evaluate();
				writeClassLevelHeader();
				writeClassName();
				writeResultMatrix();
				writeAllRules();
			}
		};
	}

	protected Result getResultType(String testName) {
		if (!resultMap.containsKey(testName))
			return Result.ERR;
		return resultMap.get(testName);
	}

	protected void writeClassLevelHeader() {
		writeLine("***********************************************");
		writeLine("***                                         ***");
		writeLine("***          T E S T S U M M A R Y          ***");
		writeLine("***                                         ***");
		writeLine("***********************************************");
		writeLine("");
	}

	protected void writeClassName() {
		writeLine("* TEST CLASS *");
		writeLine("  '{}'", ruleSet.getTargetObject().getClass());
		writeLine("");
	}

	private void writeLine(String s, Object... params) {
		this.writer.writeLine(klazz(), 0, String.format(s.replaceAll("\\{\\}", "%s"), params));
	}

	protected void writeAllRules() {
		writeLine("* ALL TEST RULES *");
		writeLine("  #   T/  F   PREDICATE");
		ruleSet.printOutClassLevelResult(klazz());
		writeLine("");
	}

	protected void writeResultMatrix() {
		writeLine("* TEST RESULT MATRIX *");
		int registeredIds = ruleSet.registeredIds();
		String[] headers = new String[ruleSet.maxLevel() + 1];
		for (int i = 0; i < headers.length; i++) {
			headers[i] = String.format("     %-30s", "LEVEL " + i + " PREDICATE");
		}
		for (int j = 0; j < registeredIds; j++) {
			for (int i = 0; i < headers.length; i++) {
				if (i == ruleSet.levelOf(j))
					headers[i] += String.format("%02d ", j);
				else
					headers[i] += "   ";
			}
		}
		for (String h : headers)
			writeLine(h);
		String line;
		for (String testName : matrix.testNames()) {
			line = String.format("[%-3s]%-30s", getResultType(testName), testName);
			for (int objId = 0; objId < registeredIds; objId++) {
				String f;
				if (isError(testName, objId)) {
					f = "E";
				} else {
					if (matrix.hasEntry(testName, objId)) {
						f = matrix.get(testName, objId) ? "T" : "F";
					} else {
						f = "-";
					}
				}
				if (ruleSet.isLeaf(objId) && !"-".equals(f) && !"T".equals(f)) {
					line += String.format("<%s>", f);
				} else {
					line += String.format(" %s ", f);
				}
			}
			writeLine(line);
		}
		writeLine("");
	}

	public void failed(String testName, int objId) {
		matrix.set(testName, objId, false);
	}

	public void passed(String testName, int objId) {
		matrix.set(testName, objId, true);
	}

	public void setRuleSet(RuleSet ruleSet) {
		this.ruleSet = ruleSet;
	}

	private Class<?> klazz() {
		return ruleSet.getTargetObject().getClass();
	}

	@Override
	public int passes(int objId) {
		return this.matrix.count(objId, true);
	}

	@Override
	public int fails(int objId) {
		return this.matrix.count(objId, false);
	}

	@Override
	public void error(String methodName) {
		this.matrix.add(methodName);
		this.resultMap.put(methodName, Result.ABT);
	}

	@Override
	public void ok(String methodName) {
		this.matrix.add(methodName);
		this.resultMap.put(methodName, Result.OK);
	}

	@Override
	public void ng(String methodName) {
		this.matrix.add(methodName);
		this.resultMap.put(methodName, Result.NG);
	}

	@Override
	public void error(String testName, int objId) {
		this.errorObjects.add(testName + ":" + objId);
	}

	private boolean isError(String testName, int objId) {
		return this.errorObjects.contains(testName + ":" + objId);
	}
}
