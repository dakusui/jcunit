package com.github.dakusui.jcunit.auto;

import java.lang.reflect.Field;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.Out.Verifier;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.pred.BasePredicate;

/**
 * 
 * * Working directory.
 * 
 * @author hiroshi
 *
 */
public class AutoRuleSet extends RuleSet implements TestRule {
	/**
	 * Name of the test run. This member is set by 'apply' method of this class.
	 */
	private String testName;
	private String[] fieldNames;
	
	/**
	 * Creates an object of this class.
	 * 
	 * {@inheritDoc}
	 * 
	 * @param context A context object.
	 * @param target An object under this test run.
	 * @param testName A test name object.
	 * @throws JCUnitException An internal procedure is failed. Usually not thrown.
	 */
	public AutoRuleSet(Context context, Object target, String... fieldNames) {
		super(context, target);
		this.fieldNames = fieldNames;
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Statement apply(final Statement base, final Description desc) {
		////
		// Since the number of test cases in one test class can be very big,
		// I would like to create one directory for one class and then store
		// all the test cases in it as sub directories.
		// So, now I'm using '/' instead of '#'.
		this.testName = desc.getClassName() + "/" + desc.getMethodName();
		try {
			Object fields = fieldNames.length == 0 ? outFieldNames() : fieldNames;
			Context c = getContext(); 
			int numFields = Basic.length(fields); 
			if (numFields == 0) {
				incase(true, c.progn(String.format("*** NO FIELD IS SPECIFIED BY TEST %s ***", this.testName), false));
			} else {
				for (int i = 0; i < Basic.length(fields); i++) {
					Object fieldName = Basic.get(fields, i);
					if (isStored(fieldName)) {
						incase(
								c.any(),
								verify().bind(
										load(fieldName), 
										c.get(this.getTargetObject(), fieldName),
										verifierForField(fieldName)
								) 
						);
					} else {
						////
						// If a field isn't stored yet, the value of it will be stored,
						// while the entire ruleSet will be failed.
						// Since this is ruleSet, even if one 'incase' clause is evaluated 
						// and failed, 
						// the following rules ('incase' clauses and 'otherwise' clause)
						// will be evaluated.
						incase(c.any(), new Store().bind(getTestName(), this.getTargetObject(), fieldName));
					}
				}
				////
				// If no field in the target object (SUT) is stored, the rule will be
				// considered 'fail'.
				this.otherwise(false);
			}
		} catch (CUT cut) {
			String msg = String.format(
					"A cut, which shouldn't be thrown during AutoRuleSet#init is being executed, was thrown. ('%s')",
					cut.getMessage()
			);
			throw new JCUnitRuntimeException(msg, cut);
		} catch (JCUnitException e) {
			String msg = String.format(
					"A JCUnitException, which shouldn't be thrown during AutoRuleSet#init is being executed, was thrown. ('%s')",
					e.getMessage()
			);
			throw new JCUnitRuntimeException(msg, e);
		}
		return super.apply(base, desc);
	}

	/*
	 * Returns a verifier object for the specified field.
	 */
	private Verifier verifierForField(Object fieldName) {
		if (fieldName == null) throw new NullPointerException();
		String s = fieldName.toString();
		Verifier ret = null;
		try {
			Field f = this.getTargetObject().getClass().getField(s);
			Out out = f.getAnnotation(Out.class);
			Class<? extends Verifier> klazz = out.verifier();
			try {
				ret = klazz.newInstance();
			} catch (InstantiationException e) {
				String msg = String.format(
						"'%s' must have public constructor with no parameter.", 
						klazz.getCanonicalName()
				);
				throw new RuntimeException(msg, e); 
			} catch (IllegalAccessException e) {
				String msg = String.format(
						"'%s' must have public constructor with no parameter.", 
						klazz.getCanonicalName()
				);
				throw new RuntimeException(msg, e); 
			} 
		} catch (SecurityException e) {
			////
			// Since only valid field name is passed as 'fieldName' this path
			// shouldn't be executed.
			throw new RuntimeException(e); 
		} catch (NoSuchFieldException e) {
			////
			// Since only valid field name is passed as 'fieldName' this path
			// shouldn't be executed.
			throw new RuntimeException(e); 
		}
		return ret;
	}
	
	/**
	 * Returns a S expression list of field names in the target object.
	 * 
	 * @return S expression list of field names.
	 * @throws JCUnitException Failed to get field names.
	 * @throws CUT Operation is cut. Usually not thrown.
	 */
	protected Object outFieldNames() throws JCUnitException, CUT {
		return Basic.eval(this.getContext(), new OutFieldNames().bind(this.getTargetObject()));
	}
	
	/**
	 * Checks if the specified field is already stored in the working directory.
	 * 
	 * @param fieldName Name of the field.
	 * @return true - if the field is stored / false - the field is not stored.
	 * @throws JCUnitException Failed to get field names.
	 * @throws CUT Operation is cut. Usually not thrown.
	 */
	protected boolean isStored(Object fieldName) throws JCUnitException, CUT {
		return Basic.evalp(this.getContext(), new IsStored().bind(this.getTestName(), this.getTargetObject(), fieldName));
	}
	
	protected Form verify() {
		Form ret = new BasePredicate() {
			private static final long serialVersionUID = 3468222886217602972L;
			
			@Override
			protected FormResult evaluateLast(Context context,
					Object[] evaluatedParams, FormResult lastResult)
					throws JCUnitException, CUT {
				Object expected = Basic.get(evaluatedParams, 0);
				Object actual = Basic.get(evaluatedParams, 1);
				Verifier v = (Verifier) Basic.get(evaluatedParams, 2);
				lastResult.value(v.verify(expected, actual));
				return lastResult;
			}
			
			@Override
			public String name() {
				return "verify";
			}
		};
		return ret;
	}

	/**
	 * Loads the value of the field from the working directory.
	 * If the value isn't stored, <code>JCUnitException</code> will be thrown.
	 * 
	 * @param fieldName Name of the field.
	 * @return Value of the field.
	 * @throws JCUnitException Failed to get field names.
	 * @throws CUT Operation is cut. Usually not thrown.
	 */
	protected Object load(Object fieldName) throws JCUnitException, CUT {
		return new Load().bind(this.getTestName(), this.getTargetObject(), fieldName);
	}

	/**
	 * Stores value of the field specified by <code>fieldName</code> to a working directory.
	 *  
	 * @param fieldName Name of the field.
	 * @throws JCUnitException Failed to store the value.
	 * @throws CUT Operation is cut. Usually not thrown.
	 */
	protected void store(Object fieldName) throws JCUnitException, CUT {
		Basic.eval(this.getContext(), new Store().bind(this.getTestName(), this.getTargetObject(), fieldName));
	}
	
	/**
	 * Returns test method name currently being executed.
	 * 
	 * @return test method name.
	 */
	protected String getTestName() {
		return this.testName;
	}
}
