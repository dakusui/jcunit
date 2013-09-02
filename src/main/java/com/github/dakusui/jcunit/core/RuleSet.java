package com.github.dakusui.jcunit.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;

public class RuleSet implements TestRule {
	public static class ReportWriter {
		public void writeLine(int indentLevel, String str) {
			String indent = "";
			for (int i = 0; i < indentLevel; i++) {
				indent += "  ";
			}
			LOGGER.info(indent + str);
		}
	}
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
		private List<RuleSetReport> ruleSetReport;
		public Report(int indentLevel, ReportWriter writer) {
			this.indent = indentLevel;
			this.writer = writer;
		}
		public boolean check(Object cond, boolean result) {
			if (result) {
				for (RuleSetReport r : this.ruleSetReport) r.matched(cond);
				writeLine(this.indent, "MATCHED:" + Basic.tostr(cond));
			} else {
				for (RuleSetReport r : this.ruleSetReport) r.notMatched(cond);
				writeLine(this.indent, "NOT MATCHED:" + Basic.tostr(cond));
			}
			if (result) this.indent ++;
			return result;
		}
		public boolean expect(Object nested, boolean result) {
			if (result) {
				
				writeLine(this.indent, "PASS:" + Basic.tostr(nested));
			} else {
				writeLine(this.indent, "FAIL:" + Basic.tostr(nested));
			}
			return result;
		}
		
		public void writeLine(int indentLevel, String str) {
			writer.writeLine(indentLevel, str);
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
		public boolean cut() {
			return this.cut;
		}
	}

	private Context context;
	private List<Pair> rules = new LinkedList<Pair>();
	private Pair otherwise = null;
	
	/**
	 * A logger object.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RuleSet.class);
	
	private static final ReportWriter writer = new ReportWriter();

	private Map<Field, Object> inValues = null;

	private String failedReason = null;

	private Map<Field, Object> outValues;
	private Object target;
	private LinkedList<RuleSetReport> ruleSetReports;

	public RuleSet(Context context) {
		////
		// On what conditions can context and target be different? 
		this.context = context;
	}

	@Override
	public Statement apply(final Statement base, final Description desc) {
		for (Field f : this.target.getClass().getFields()) {
			if (f.getAnnotation(ClassRule.class) instanceof RuleSetReport) {
				try {
					ruleSetReports.add((RuleSetReport) f.get(this.target));
				} catch (IllegalArgumentException e) {
					assert false;
					throw new RuntimeException();
				} catch (IllegalAccessException e) {
					String msg = String.format(
							"The field '%s' of class '%s' must be public.", 
							f.getName(), 
							this.target.getClass()
					);
					throw new ObjectUnderFrameworkException(msg, e);
				}
			}
		}
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				boolean evaluated = false;
				try {
					writer.writeLine(0, "***********************************************");
					writer.writeLine(0, "***                                         ***");
					writer.writeLine(0, "***           T E S T R E P O R T           ***");
					writer.writeLine(0, "***                                         ***");
					writer.writeLine(0, "***********************************************");
					writer.writeLine(0, "");
					writer.writeLine(0, "* TEST NAME *");
					writer.writeLine(1, String.format(
							"'%s.%s'",
							desc.getClassName(),
							desc.getMethodName())
					);
					writer.writeLine(0, "");
					
					base.evaluate();
					evaluated = true;
				} finally {
					if (evaluated) {
						RuleSet.this.setOutValues(composeOutValues(target));
						boolean passed = verify(target, RuleSet.this);
						writer.writeLine(0, "");
						String msg = "Test:" + RuleSet.summarize(inValues) + " was failed. (" + failedReason() + ")";
						TestCase.assertTrue(msg, passed);
					}
				}
			}
		};
	}

	protected boolean verify(Object target, RuleSet ruleSet) throws JCUnitException, CUT {
		assert this.inValues != null;
		
		if (LOGGER.isInfoEnabled()) {
			writer.writeLine(0, "* INPUT VALUES *");
			dumpValues(writer, this.inValues);
			writer.writeLine(0, "");
			writer.writeLine(0, "* OUTPUT VALUES *");
			dumpValues(writer, this.outValues);
			writer.writeLine(0, "");
		}

		boolean ret = false;
		writer.writeLine(0, "* RULES *");
		Report report = new Report(1, writer);
		report.ruleSetReport = this.ruleSetReports;
		try {
			ret = this.apply(report);
			if (!ret) failedReason("Rule matched but failed.");
		} catch (RuleIgnored e) {
			failedReason("No rule matched with this test.");
			return false;
		} finally {
			if (!ret) {
				LOGGER.error("");
				LOGGER.error("  FAIL:{}", failedReason());
				LOGGER.error("");
				writer.writeLine(0, "* EXCEPTIONS *");
				dumpExceptions(writer, this.outValues);
			}
		}
		return ret;
	}

	protected void dumpValues(ReportWriter writer, Map<Field, Object> values) {
		writer.writeLine(1, String.format("VALUES(%d)", values.size()));
		List<Field> keys = new ArrayList<Field>(values.keySet());
		Collections.sort(keys, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Field key : keys) {
			Object v = values.get(key);
			writer.writeLine(2, String.format("%s:%s(%s)", key.getName(), v, key.getType().getName()));
		}
	}
	
	protected void dumpExceptions(ReportWriter writer, Map<Field, Object> values) {
		List<Field> keys = new ArrayList<Field>(values.keySet());
		Collections.sort(keys, new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (Field key : keys) {
			Object v = values.get(key);
			if (v instanceof Throwable) {
				Throwable t = (Throwable)v;
				writer.writeLine(1, String.format("%s:%s(%s)", key.getName(), v, key.getType().getName()));
				writer.writeLine(2, t.getMessage());
				for (StackTraceElement ste : t.getStackTrace()) {
					writer.writeLine(3, ste.toString());
				}
				writer.writeLine(1, "");
			}
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

	public static Map<Field, Object> composeOutValues(Object out) {
		if (out == null) throw new NullPointerException();
		Map<Field, Object> ret = new HashMap<Field, Object>();
		for (Field f : Utils.getOutFieldsFromClassUnderTest(out.getClass())) {
			ret.put(f, Utils.getFieldValue(out, f));
		}
		return ret;
		
	}

	/**
	 * Adds a pair of a 'condition' and 'expectation' which should be satisfied
	 * if the 'condition' is evaluated <code>true</code> to this object.
	 * Pairs are evaluated in the order where they are added. And <code>expect</code>
	 * can be another <code>RuleSet</code> object, which will be evaluated
	 * in a nested manner.
	 * If this object is already closed, i.e. <code>otherwise</code> method is
	 * called on this object, an <code>IllegalStateException</code> will be thrown. 
	 * @param condition must be a boolean value or a predicate.
	 * @param expect must be a boolean value, a predicate, or a <code>RuleSet</code> object.
	 * @return this object.
	 * @see RuleSet#apply(Report)
	 */
	public RuleSet incase(Object condition, Object expect) {
		if (this.otherwise != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		this.rules.add(new Pair(condition, expect, false));
		return this;
	}
	
	public RuleSet incase(Object condition) {
		if (this.otherwise != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() > 0) {
			Pair lastPair = this.rules.get(this.rules.size() - 1); 
			////
			// you must call 'expect' in this case.
			if (lastPair.cond != null && lastPair.nested == null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		}
		this.rules.add(new Pair(condition, null, false));
		return this;
	}
	
	public RuleSet expect(Object expect) {
		if (this.otherwise != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() == 0) throw new IllegalStateException("FRIENDLY MESSAGE!");
		Pair lastPair = this.rules.get(this.rules.size() - 1); 
		if (lastPair.nested != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		lastPair.nested = expect;
		
		return this;
	}

	public RuleSet cut() {
		if (this.otherwise != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.rules.size() == 0) throw new IllegalStateException("FRIENDLY MESSAGE!");
		Pair lastPair = this.rules.get(this.rules.size() - 1); 
		if (lastPair.nested == null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (lastPair.cut) throw new IllegalStateException("FRIENDLY MESSAGE!");
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
	 * @see RuleSet#apply(Report)
	 */
	public RuleSet otherwise(Object expect) {
		if (this.rules.size() < 1) throw new IllegalStateException("FRIENDLY MESSAGE!");
		if (this.otherwise != null) throw new IllegalStateException("FRIENDLY MESSAGE!");
		this.otherwise = new Pair(true, expect, false);
		return this;
	}
	
	public boolean apply(Report report) throws JCUnitException, RuleIgnored, CUT {
		boolean passed = true;
		boolean matchedAtLeastOnce = false;
		int indentLevel = report.indent;
		for (Pair cur : rules) {
			////
			// reset the indentation level.
			report.indent = indentLevel;
			if (report.check(cur.cond, Basic.evalp(this.context, cur.cond))) {
				matchedAtLeastOnce = true;
				if (cur.nested instanceof RuleSet) {
					RuleSet nested = (RuleSet) cur.nested;
					try {
						passed &= nested.apply(report);
						if (cur.cut()) break;
					} catch (RuleIgnored e) {
						passed = false;
						if (e.source() == nested) continue;
					}
				} else {
					passed &= report.expect(cur.nested, Basic.evalp(context, cur.nested));
					if (cur.cut()) break;
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
				report.check("(otherwise)", true);
				if (this.otherwise.nested instanceof RuleSet) {
					matchedAtLeastOnce = true;
					RuleSet nested = (RuleSet) this.otherwise.nested;
					return nested.apply(report);
				} else if (this.otherwise.nested != null){
					matchedAtLeastOnce = true;
					return report.expect(this.otherwise.nested, Basic.evalp(context, this.otherwise.nested));
				}
			}
		}
		throw new RuleIgnored(this);
	}

	private static String summarize(Map<Field, Object> values) {
		Map<String, Object> map = new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
	
			public String toString() {
		        Iterator<Entry<String,Object>> i = entrySet().iterator();
		        if (! i.hasNext())
		            return "{}";
	
		        StringBuilder sb = new StringBuilder();
		        sb.append('{');
		        for (;;) {
		            Entry<String,Object> e = i.next();
		            String key = e.getKey();
		            Object value = e.getValue();
		            sb.append(key);
		            sb.append('=');
		            value = value instanceof Object[] ? ArrayUtils.toString(value)
		            		                           : value;
		            sb.append(value == this ? "(this Map)" : value);
		            if (! i.hasNext())
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

	public void setTarget(Object cut) {
		this.target = cut;
	}
}
