package com.github.dakusui.jcunit.framework.examples.misc;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.factor.LevelsFactoryBase;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;

import java.lang.reflect.Field;

/**
 */
public class CompositeLevelsFactory<T> extends LevelsFactoryBase<T> {
	@Override
	protected void init(Field targetField, FactorField annotation, Object[] parameters) {
		Utils.checknotnull(targetField);
		Factors factors = buildFactorsFromClass((Class<T>) targetField.getType());

		IPO2SchemafulTupleGenerator generator = new IPO2SchemafulTupleGenerator();
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
