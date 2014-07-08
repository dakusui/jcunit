package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.annotations.FactorField;
import com.github.dakusui.jcunit.core.annotations.FactorField.Levels;
import com.github.dakusui.jcunit.core.factorfactories.DefaultFactorFactory;

public class Sample {
	public static final int a = 0;
	public static final int[] b = {1, 2, 3};

	@FactorField(@Levels(factory = DefaultFactorFactory.class))
	int test;
}
