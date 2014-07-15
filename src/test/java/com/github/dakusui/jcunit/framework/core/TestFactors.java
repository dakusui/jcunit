package com.github.dakusui.jcunit.framework.core;

import com.github.dakusui.jcunit.core.factor.FactorField;

/**
 */
public class TestFactors {
	@FactorField
	public int validIntFieldWithDefaultValues;
	@FactorField(intLevels = {1, 2, 3})
	public int validIntFieldWithExplicitIntValues;
	@FactorField(longLevels = {1, 2, 3})
	public int invalidIntFieldWithExplicitLongValues;
}
