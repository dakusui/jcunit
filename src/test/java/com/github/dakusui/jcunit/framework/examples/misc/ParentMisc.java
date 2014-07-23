package com.github.dakusui.jcunit.framework.examples.misc;

import com.github.dakusui.jcunit.core.factor.FactorField;

/**
 */
public class ParentMisc {
	@FactorField
	public int no;
	@FactorField(levelsFactory = CompositeLevelsFactory.class)
	public ChildMisc child;
}
