package com.github.dakusui.jcunit.examples.misc;

import com.github.dakusui.jcunit.core.FactorField;

/**
 */
public class ParentMisc {
	@FactorField
	public int no;
	@FactorField(levelsFactory = CompositeLevelsProvider.class)
	public ChildMisc child;
}
