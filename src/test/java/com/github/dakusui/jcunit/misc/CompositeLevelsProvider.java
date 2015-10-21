package com.github.dakusui.jcunit.misc;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;

import java.lang.reflect.Field;

/**
 * This class is not yet finished.
 */
public class CompositeLevelsProvider<T> extends LevelsProviderBase {
	@Override
	protected void init(Field targetField, FactorField annotation, Object[] parameters) {
		Checks.checknotnull(targetField);
		Factors factors = buildFactorsFromClass((Class<T>) targetField.getType());

		IPO2TupleGenerator generator = new IPO2TupleGenerator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public T get(int index) {
		return null;
	}

	public static <T> Factors buildFactorsFromClass(Class<T> clazz) {
		return null;
	}
}
