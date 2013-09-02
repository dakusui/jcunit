package com.github.dakusui.jcunit.core;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RuleSetReport implements TestRule {

	public Statement apply(Statement base, Description description) {
		return statement(base);
	}

	private Statement statement(final Statement base) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				base.evaluate();
			}
		};
	}

	public void matched(Object cond) {
		// TODO Auto-generated method stub
		
	}

	public void notMatched(Object cond) {
		// TODO Auto-generated method stub
		
	}

	
}
