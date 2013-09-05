package com.github.dakusui.jcunit.core;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Summarizer implements TestRule {
	private static Logger LOGGER = Logger.getLogger(Summarizer.class);
	
	private static class Matrix {
		Map<String, Map<Integer, Boolean>> map = new LinkedHashMap<String, Map<Integer, Boolean>>();
		////
		// The order where object id's are coming in isn't sort, the client
		// side needs to sort it again either way. So, I don't use LinkedHashSet
		// here.
		Set<Integer> objIds = new HashSet<Integer>();
		
		public void set(String testName, int objId, boolean result) {
			if (!map.containsKey(testName)) {
				map.put(testName, new LinkedHashMap<Integer, Boolean>());
			}
			map.get(testName).put(objId, result);
			objIds.add(objId);
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
		public Set<Integer> objectIds() {
			return objIds;
		}
		public Set<String> testNames() {
			return map.keySet();
		}
	}
	
	Matrix matrix = new Matrix();
	private RuleSet ruleSet;
	
	public Statement apply(Statement base, Description description) {
		return statement(base);
	}

	private Statement statement(final Statement base) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				base.evaluate();
				
				String header = "                    ";
				int registeredIds = ruleSet.registeredIds();
				for (int i = 0; i < registeredIds; i++) {
					header += String.format("%02d ", i);
				}
				LOGGER.info(header);
				String line; 
				for (String testName : matrix.testNames()) {
					line = String.format("%-20s", testName);
					for (int objId = 0; objId < registeredIds; objId++) {
						String f;
						if (matrix.hasEntry(testName, objId)) {
							f = matrix.get(testName, objId) ? "T" : "F";
						} else {
							f = "-";
						}
						line += String.format(" %s ", f);
					}
					LOGGER.info(line);
				}
			}
		};
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

}
