package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.report.ReportWriter;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

/**
 * A class that represents a set of test rules.
 * 
 * 
 * @author hiroshi
 * 
 */
public class RuleSet implements TestRule {
	private static final String OTHERWISECLAUSE_FORMAT = "(otherwise-%d)";
	private static final Summarizer DUMMYSUMMARIZER = new Summarizer() {
		@Override
		public void passed(String testName, int id) {
		}

		@Override
		public void failed(String testName, int id) {
		}

		@Override
		public void setRuleSet(RuleSet ruleSet) {
		}

		@Override
		public Statement apply(Statement base, Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
				}
			};
		}

		@Override
		public int passes(int objId) {
			return -1;
		}

		@Override
		public int fails(int objId) {
			return -1;
		}

		@Override
		public void error(String methodName) {
		}

		@Override
		public void ok(String methodName) {
		}

		@Override
		public void ng(String methodName) {
		}

		@Override
		public void error(String string, int id) {
		}
	};

	public static class RuleIgnored extends JCUnitException {
		/**
		 * A serial version UID.
		 */
		private static final long serialVersionUID = 1L;
		private RuleSet source;

		RuleIgnored(RuleSet jcunitRuleSet) {
			this.source = jcunitRuleSet;
		}

		RuleSet source() {
			return this.source;
		}
	}

	public class Report {
		int indent = 0;
		private ReportWriter writer;
		private Description desc;

		public Report(Description desc, int indentLevel, ReportWriter writer) {
			this.indent = indentLevel;
			this.writer = writer;
			this.desc = desc;
		}

		public boolean check(String testName, Object cond, boolean result) {
			String id = String.format("[%02d]", idOf(cond));
			Summarizer s = RuleSet.this.summarizer;
			if (result) {
				s.passed(testName, idOf(cond));
				writeLine(this.desc, this.indent, id + "MATCHED:" + Basic.tostr(cond));
			} else {
				s.failed(testName, idOf(cond));
				writeLine(this.desc, this.indent, id + "NOT MATCHED:" + Basic.tostr(cond));
			}
			if (result)
				this.indent++;
			return result;
		}

		public boolean expect(String testName, Object nested, boolean result) {
			String id = String.format("[%02d]", idOf(nested));
			Summarizer s = RuleSet.this.summarizer;
			if (result) {
				s.passed(testName, idOf(nested));
				writeLine(this.desc, this.indent, id + "PASS:" + Basic.tostr(nested));
			} else {
				s.failed(testName, idOf(nested));
				writeLine(this.desc, this.indent, id + "FAIL:" + Basic.tostr(nested));
			}
			return result;
		}

		public void writeLine(Description desc, int indentLevel, String str) {
			writer.writeLine(this.desc, indentLevel, str);
		}
	}

	private static class Pair {
		Object cond;
		Object nested;
		boolean cut;

		Pair(Object cond, Object nested, boolean cut) {
			this.cond = cond;
			this.nested = nested;
			this.cut = cut;
		}

		boolean cut() {
			return this.cut;
		}
	}

	private Context context;
	private List<Pair> rules = new ArrayList<Pair>();
	private Pair otherwise = null;

	protected static final ReportWriter writer = new ReportWriter();

	private Map<Field, Object> inValues = null;

	private String failedReason = null;

	private Map<Field, Object> outValues;
	private Object target;

	private Map<Object, Integer> idMap = null; //new IdentityHashMap<Object, Integer>();
	private Map<Object, Integer> levelMap = null; //new HashMap<Object, Integer>();
	private Summarizer summarizer = DUMMYSUMMARIZER;
	private int maxLevel;
	private Set<Integer> leaves;

	public RuleSet(Context context, Object target) {
		////
		// On what conditions can context and target be different?
		this.context = context;
		this.target = target;
	}

	@Override
	public Statement apply(final Statement base, final Description desc) {
		this.idMap = new IdentityHashMap<Object, Integer>();
		this.levelMap = new HashMap<Object, Integer>();
		this.leaves = new HashSet<Integer>();
		identifyObjectsAndSetSummarizer(this.summarizer, idMap, 0, levelMap, 0, this.leaves);
		this.maxLevel = 0;
		for (int l : this.levelMap.values()) {
			this.maxLevel = Math.max(this.maxLevel, l);
		}
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				Throwable throwable = null;
				try {
					initTestCaseLevelReport(desc);
					writeTestCaseLevelHeader(desc);
					writeTestCaseId(desc);
					try {
						base.evaluate();
					} catch (Throwable t) {
						throwable = t;
						throw t;
					}
				} finally {
					writeInputValues(desc);
					if (throwable == null) {
						boolean verified = false;
						boolean ok = false;
						boolean verificationExecuted = false;
						try {
							ok = verify(desc, target, RuleSet.this, desc.getMethodName());
							verificationExecuted = true;
							verified = true;
						} finally {
							RuleSet.this.setOutValues(composeOutValues());
							writeResult(desc, ok);
							writeOutputValues(desc);
							String msg = "Test:" + RuleSet.summarize(inValues) + " was failed. (" + failedReason() + ")";
							if (ok)
								RuleSet.this.summarizer.ok(desc.getMethodName());
							else
								RuleSet.this.summarizer.ng(desc.getMethodName());
							if (!verified) {
								writer.writeErrorLine(desc, 0, "");
								String failedReason = failedReason();
								writer.writeErrorLine(desc, 0, String.format("  FAIL:%s", failedReason == null ? "(not available)" : failedReason));
								writer.writeErrorLine(desc, 0, "");
								writer.writeLine(desc, 0, "* EXCEPTIONS *");
								dumpExceptions(desc, writer, RuleSet.this.outValues);
							}
							if (verificationExecuted) {
								TestCase.assertTrue(msg, ok);
							}
						}
					} else {
						dumpException(throwable);
						writer.writeLine(desc, 0, "");
						RuleSet.this.summarizer.error(desc.getMethodName());
					}
				}
			}

			protected void dumpException(Throwable t) {
				writer.writeErrorLine(desc, 0, "* TEST ABORTED *");
				writer.writeLine(desc, 1, String.format("%s", t));
				for (StackTraceElement ste : t.getStackTrace()) {
					writer.writeLine(desc, 2, ste.toString());
				}
			}
		};
	}

	protected boolean verify(Description desc, Object target, RuleSet ruleSet, String testName) throws JCUnitException, CUT {
		assert this.inValues != null;

		boolean ret = false;
		writer.writeLine(desc, 0, "* RULES *");
		Report report = new Report(desc, 1, writer);
		try {
			ret = this.apply(report, testName);
			if (!ret)
				failedReason("Rule matched but failed.");
		} catch (RuleIgnored e) {
			failedReason("No rule matched with this test.");
			return false;
		} finally {
			writer.writeLine(desc, 0, "");
		}
		return ret;
	}

	private int identifyObjectsAndSetSummarizer(Summarizer summarizer, Map<Object, Integer> idMap, int i, Map<Object, Integer> levelMap, int j, Set<Integer> leaves) {
		this.summarizer = summarizer;
		this.leaves = leaves;
		this.idMap = idMap;
		this.levelMap = levelMap;
		List<Pair> rules = new LinkedList<Pair>();
		rules.addAll(this.rules);
		if (this.otherwise != null)
			rules.add(this.otherwise);
		for (Pair p : rules) {
			if (OTHERWISECLAUSE_FORMAT.equals(p.cond)) {
				p.cond = String.format(p.cond.toString(), i);
			}
			levelMap.put(p.cond, j);
			idMap.put(p.cond, i++);
			if (p.nested instanceof RuleSet) {
				i = ((RuleSet) p.nested).identifyObjectsAndSetSummarizer(summarizer, idMap, i, levelMap, j + 1, leaves);
			} else {
				levelMap.put(p.nested, j + 1);
				leaves.add(i);
				idMap.put(p.nested, i++);
			}
		}
		return i;
	}

	int idOf(Object obj) {
		if (!idMap.containsKey(obj)) {
			assert false;
			return -1;
		}
		return idMap.get(obj);
	}

	int levelOf(Object obj) {
		if (!levelMap.containsKey(obj)) {
			assert false;
			return -1;
		}
		return levelMap.get(obj);
	}

	int levelOf(int objectId) {
		return levelOf(obj(objectId));
	}

	private Object obj(int objectId) {
		for (Entry<Object, Integer> ent : this.idMap.entrySet()) {
			int i = ent.getValue();
			if (i == objectId) {
				return ent.getKey();
			}
		}
		return null;
	}

	boolean isLeaf(int objectId) {
		return this.leaves.contains(objectId);
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

	protected void dumpExceptions(Description desc, ReportWriter writer, Map<Field, Object> values) {
		List<Field> keys = new ArrayList<Field>(values.keySet());
		Collections.sort(keys, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		boolean atLeastOneException = false;
		for (Field key : keys) {
			Object v = values.get(key);
			if (v instanceof Throwable) {
				Throwable t = (Throwable) v;
				writer.writeLine(desc, 1, String.format("%s:%s(%s)", key.getName(), v, key.getType().getName()));
				writer.writeLine(desc, 2, t.getMessage());
				for (StackTraceElement ste : t.getStackTrace()) {
					writer.writeLine(desc, 3, ste.toString());
				}
				writer.writeLine(desc, 1, "");
				atLeastOneException = true;
			}
		}
		if (!atLeastOneException) {
			writer.writeLine(desc, 1, "(none)");
		}
	}

	public String failedReason() {
		return this.failedReason;
	}

	public void failedReason(String reason) {
		this.failedReason = reason;
	}

	public void setInValues(Map<Field, Object> inValues) {
		this.inValues = inValues;
	}

	public void setOutValues(Map<Field, Object> outValues) {
		this.outValues = outValues;
	}

	private Map<Field, Object> composeOutValues() {
		Map<Field, Object> ret = new HashMap<Field, Object>();
		for (Field f : getOutFields()) {
			ret.put(f, Utils.getFieldValue(this.target, f));
		}
		return ret;
	}

	protected Field[] getOutFields() {
		return Utils.getOutFieldsFromClassUnderTest(this.target.getClass());
	}

	/**
	 * Adds a pair of a 'condition' and 'expectation' which should be satisfied
	 * if the 'condition' is evaluated <code>true</code> to this object.
	 * Pairs are evaluated in the order where they are added. And <code>expect</code> can be another
	 * <code>RuleSet</code> object, which will be evaluated
	 * in a nested manner.
	 * If this object is already closed, i.e. <code>otherwise</code> method is
	 * called on this object, an <code>IllegalStateException</code> will be thrown.
	 * 
	 * @param condition must be a boolean value or a predicate.
	 * @param expect must be a boolean value, a predicate, or a <code>RuleSet</code> object.
	 * @return this object.
	 * @see RuleSet#apply(Report, String)
	 */
	public RuleSet incase(Object condition, Object expect) {
		if (this.otherwise != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		this.rules.add(new Pair(condition, expect, false));
		return this;
	}

	public RuleSet auto() {
		return this;
	}

	public RuleSet incase(Object condition) {
		if (this.otherwise != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() > 0) {
			Pair lastPair = this.rules.get(this.rules.size() - 1);
			////
			// you must call 'expect' in this case.
			if (lastPair.cond != null && lastPair.nested == null)
				throw new IllegalStateException("FRIENDLY MESSAGE!");
		}
		this.rules.add(new Pair(condition, null, false));
		return this;
	}

	public RuleSet expect(Object expect) {
		if (this.otherwise != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() == 0)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		Pair lastPair = this.rules.get(this.rules.size() - 1);
		if (lastPair.nested != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		lastPair.nested = expect;

		return this;
	}

	public RuleSet cut() {
		if (this.otherwise != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() == 0)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		Pair lastPair = this.rules.get(this.rules.size() - 1);
		if (lastPair.nested == null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (lastPair.cut)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		lastPair.cut = true;
		return this;
	}

	/**
	 * After this method is called, <code>incase</code> method and this method
	 * won't be able to be called on this object.
	 * 
	 * @param expect must be a boolean value, a predicate, or an <code>RuleSet</code> object.
	 * @return this object
	 * @see RuleSet#incase(Object, Object)
	 * @see RuleSet#apply(Report, String)
	 */
	public RuleSet otherwise(Object expect) {
		if (this.rules.size() < 1)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.otherwise != null)
			throw new IllegalStateException("FRIENDLY MESSAGE!");
		this.otherwise = new Pair("(otherwise-%d)", expect, false);
		return this;
	}

	private boolean evalp(Object p, String testName) throws JCUnitException, CUT {
		try {
			return Basic.evalp(context, p);
		} catch (JCUnitException e) {
			throw e;
		} catch (CUT e) {
			throw e;
		} catch (RuntimeException e) {
			this.summarizer.error(testName, idOf(p));
			throw e;
		} catch (Error e) {
			this.summarizer.error(testName, idOf(p));
			throw e;
		}
	}

	public boolean apply(Report report, String testName) throws JCUnitException, RuleIgnored, CUT {
		boolean passed = true;
		boolean matchedAtLeastOnce = false;
		int indentLevel = report.indent;

		for (Pair cur : rules) {
			////
			// reset the indentation level.
			report.indent = indentLevel;
			if (report.check(testName, cur.cond, evalp(cur.cond, testName))) {
				matchedAtLeastOnce = true;
				if (cur.nested instanceof RuleSet) {
					RuleSet nested = (RuleSet) cur.nested;
					try {
						passed &= nested.apply(report, testName);
						if (cur.cut())
							break;
					} catch (RuleIgnored e) {
						passed = false;
						if (e.source() == nested)
							continue;
					}
				} else {
					passed &= report.expect(testName, cur.nested, evalp(cur.nested, testName));
					if (cur.cut())
						break;
				}
			}
		}
		////
		// reset the indentation level.
		report.indent = indentLevel;
		if (matchedAtLeastOnce) {
			return passed;
		} else {
			if (this.otherwise != null) {
				report.check(testName, this.otherwise.cond, true);
				if (this.otherwise.nested instanceof RuleSet) {
					matchedAtLeastOnce = true;
					RuleSet nested = (RuleSet) this.otherwise.nested;
					return nested.apply(report, testName);
				} else if (this.otherwise.nested != null) {
					matchedAtLeastOnce = true;
					return report.expect(testName, this.otherwise.nested, evalp(this.otherwise.nested, testName));
				}
			}
		}
		throw new RuleIgnored(this);
	}

	private static String summarize(Map<Field, Object> values) {
		Map<String, Object> map = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;

			public String toString() {
				Iterator<Entry<String, Object>> i = entrySet().iterator();
				if (!i.hasNext())
					return "{}";

				StringBuilder sb = new StringBuilder();
				sb.append('{');
				for (;;) {
					Entry<String, Object> e = i.next();
					String key = e.getKey();
					Object value = e.getValue();
					sb.append(key);
					sb.append('=');
					value = value instanceof Object[] ? ArrayUtils.toString(value)
							: value;
					sb.append(value == this ? "(this Map)" : value);
					if (!i.hasNext())
						return sb.append('}').toString();
					sb.append(", ");
				}
			}
		};
		for (Field f : values.keySet()) {
			map.put(f.getName(), values.get(f));
		}
		String ret = map.toString();
		return ret;
	}

	public int registeredIds() {
		return idMap.size();
	}

	public int maxLevel() {
		return maxLevel;
	}

	public RuleSet summarizer(Summarizer summarizer) {
		summarizer.setRuleSet(this);
		this.summarizer = summarizer;
		return this;
	}

	public void printOutClassLevelResult(Class<?> klazz) {
		List<Pair> pairs = new ArrayList<Pair>(this.rules);
		if (this.otherwise != null)
			pairs.add(this.otherwise);
		for (Pair p : pairs) {
			writer.writeLine(klazz, 0,
					String.format(
							"[%02d]%3d/%3d - %s%s",
							idOf(p.cond),
							this.summarizer.passes(idOf(p.cond)),
							this.summarizer.fails(idOf(p.cond)),
							spaces(levelOf(p.cond)),
							Basic.tostr(p.cond, true)
							));
			if (p.nested instanceof RuleSet)
				((RuleSet) p.nested).printOutClassLevelResult(klazz);
			else
				writer.writeLine(klazz, 0,
						String.format(
								"[%02d]%3d/%3d - %s%s",
								idOf(p.nested),
								this.summarizer.passes(idOf(p.nested)),
								this.summarizer.fails(idOf(p.nested)),
								spaces(levelOf(p.nested)),
								Basic.tostr(p.nested, true)
								));
		}
	}

	private String spaces(int i) {
		if (i == 0)
			return "";
		return String.format("%" + (i * 2) + "s", "");
	}

	public Object getTargetObject() {
		return this.target;
	}

	public Context getContext() {
		return this.context;
	}

	/**
	 * Returns a map which associates '@Out' annotated fields and their values.
	 * 
	 * @return Map for '@Out' annotated fields and their values.
	 */
	protected Map<Field, Object> getOutValues() {
		return this.outValues;
	}

	protected void initTestCaseLevelReport(Description desc) {
		writer.deleteReport(desc);
	}

	protected void writeTestCaseLevelHeader(final Description desc) {
		writer.writeLine(desc, 0, "***********************************************");
		writer.writeLine(desc, 0, "***                                         ***");
		writer.writeLine(desc, 0, "***           T E S T R E P O R T           ***");
		writer.writeLine(desc, 0, "***                                         ***");
		writer.writeLine(desc, 0, "***********************************************");
		writer.writeLine(desc, 0, "");
	}

	protected void writeTestCaseId(final Description desc) {
		writer.writeLine(desc, 0, "* TEST NAME *");
		writer.writeLine(desc, 1, String.format(
				"'%s/%s'",
				desc.getClassName(),
				desc.getMethodName())
				);
		writer.writeLine(desc, 0, "");
	}

	protected void writeInputValues(final Description desc) {
		writer.writeLine(desc, 0, "* INPUT VALUES *");
		dumpValues(desc, writer, RuleSet.this.inValues);
		writer.writeLine(desc, 0, "");
	}

	protected void writeOutputValues(final Description desc) {
		writer.writeLine(desc, 0, "* OUTPUT VALUES *");
		dumpValues(desc, writer, RuleSet.this.outValues);
		writer.writeLine(desc, 0, "");
	}

	private void writeResult(Description desc, boolean ok) {
		writer.writeLine(desc, 0, "* RESULT *");
		writer.writeLine(desc, 1, ok ? "PASSED" : "FAILED");
		writer.writeLine(desc, 0, "");

	}
}
