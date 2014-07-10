package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters;
import com.github.dakusui.jcunit.compat.generators.SimpleTestArrayGenerator;
import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;
import com.github.dakusui.jcunit.compat.report.ReportWriter;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Suite {
	/**
	 * A report writer object.
	 */
	private static ReportWriter writer = new ReportWriter();
	private final ArrayList<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively. Do not use programmatically.
	 */
	public JCUnit(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner>emptyList());
		List<Object> parametersList = getParametersList(getTestClass());
		for (int i = 0; i < parametersList.size(); i++) {
			runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
					parametersList, i));
		}
	}

	public static FactorLoader getFactorLoader(Field inField) {
		FactorLoader ret = new FactorLoader(inField);
		return ret;
	}

	/**
	 * @param generatorClass A generator class to be used for <code>cut</code>
	 * @param params         TODO
	 * @param factors        Domain definitions for all the fields.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static TestCaseGenerator newTestArrayGenerator(
			@SuppressWarnings("rawtypes")
			Class<? extends TestArrayGenerator> generatorClass,
			GeneratorParameters.Value[] params,
			Factors factors) {
		TestCaseGenerator ret = null;
		try {
			ret = (TestCaseGenerator) generatorClass.newInstance();
			ret.init(params, factors);
		} catch (InstantiationException e) {
			throw new JCUnitPluginException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new JCUnitPluginException(e.getMessage(), e);
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private static Class<? extends TestArrayGenerator> getTestArrayGeneratorClass(
			Class<? extends Object> cuf) {
		Generator an = cuf.getAnnotation(Generator.class);
		Class<? extends TestArrayGenerator> ret = an != null ? an.value() : null;
		if (ret != null) {
			return ret;
		} else {
			Class<? extends Object> superClass = cuf.getSuperclass();
			if (superClass == null) {
				return null;
			}
			return getTestArrayGeneratorClass(superClass);
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	private List<Object> getParametersList(TestClass klass) throws Throwable {
		@SuppressWarnings("rawtypes")
		Class<? extends TestArrayGenerator> generatorClass = getTestArrayGeneratorClass(
				klass
						.getJavaClass()
		);
		if (generatorClass == null) {
			generatorClass = SimpleTestArrayGenerator.class;
		}
		return this.composeTestArray(klass.getJavaClass(), generatorClass);
	}

	/*
		 * Composes the test array.
		 */
	public List<Object> composeTestArray(
			Class<? extends Object> cut,
			@SuppressWarnings("rawtypes")
			Class<? extends TestArrayGenerator> generatorClass)
			throws JCUnitException {
		if (generatorClass == null) {
			throw new NullPointerException();
		}

		// //
		// Load parameters from class definition. If it isn't present, an empty
		// array will be given.
		GeneratorParameters paramsAnnotation = cut
				.getAnnotation(GeneratorParameters.class);
		GeneratorParameters.Value[] params = new GeneratorParameters.Value[0];
		if (paramsAnnotation != null) {
			params = paramsAnnotation.value();
		}

		// //
		// Initialize the domains for every '@In' annotated field.
		Field[] fields = Utils.getAnnotatedFields(cut, FactorField.class);
		Factors.Builder factorsBuilder = new Factors.Builder();
		List<String> errors = new LinkedList<String>();
		for (Field f : fields) {
			FactorLoader factorLoader = JCUnit.getFactorLoader(f);
			FactorLoader.ValidationResult validationResult = factorLoader.validate();
			if (!validationResult.isValid()) {
				errors.add(validationResult.getErrorMessage());
			}
			Factor factor = factorLoader.getFactor();
			factorsBuilder.add(factor);
		}

		// //
		// Instantiates the test array generator.
		TestCaseGenerator testCaseGenerator = JCUnit
				.newTestArrayGenerator(generatorClass, params, factorsBuilder.build());

		// //
		// Compose an array to be returned to the caller.
		List<Object> ret = new ArrayList<Object>();
		for (Tuple testCase : testCaseGenerator) {
			ret.add(testCase);
		}
		return ret;
	}
}
