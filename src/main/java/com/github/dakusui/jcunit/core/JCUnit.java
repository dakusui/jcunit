package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.generators.SchemafulTupleGenerator;
import com.github.dakusui.jcunit.generators.SchemafulTupleGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Suite {
	private final ArrayList<Runner> runners = new ArrayList<Runner>();

	/**
	 * Only called reflectively by JUnit. Do not use programmatically.
	 */
	public JCUnit(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner>emptyList());
		SchemafulTupleGenerator schemafulTupleGenerator = SchemafulTupleGeneratorFactory.INSTANCE
				.createSchemafulTupleGeneratorFromClass(klass);
		int id;
		JCUnitTestCaseFilter filter = createTestCaseFilter(klass);
		for (id = 0; id < schemafulTupleGenerator.size(); id++) {
			if (filter.shouldBeExecuted(id)) {
				runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
						id, JCUnitTestCaseType.Normal, new LinkedList<Serializable>(), schemafulTupleGenerator.get(id)));
			}
		}
		ConstraintManager cm = schemafulTupleGenerator.getConstraintManager();
		final List<LabeledTestCase> violations = cm.getViolations();
		for (LabeledTestCase violation : violations) {
			if (filter.shouldBeExecuted(id)) {
				runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
						id, JCUnitTestCaseType.Violation, violation.getLabels(),
						violation.getTestCase()));
			}
			id++;
		}
		if (hasParametersMethod()) {
			for (LabeledTestCase testCase : allCustomTestCases()) {
				if (filter.shouldBeExecuted(id)) {
					runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
							id, JCUnitTestCaseType.Custom, testCase.getLabels(), testCase.getTestCase()));
				}
				id++;
			}
		}
	}

	private JCUnitTestCaseFilter createTestCaseFilter(Class<?> klass) {
		if (klass.isAnnotationPresent(TestExecution.class)) {
			return JCUnitTestCaseFilter.createTestCaseFilter(klass.getAnnotation(TestExecution.class));
		} else {
			return JCUnitTestCaseFilter.createTestCaseFilter();
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private Iterable<LabeledTestCase> allCustomTestCases() throws Throwable {
		Object parameters = getParametersMethod().invokeExplosively(null);
		if (parameters instanceof Iterable) {
			return (Iterable<LabeledTestCase>) parameters;
		} else {
			throw parametersMethodReturnedWrongType();
		}
	}

	private boolean hasParametersMethod() {
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
				Parameters.class);
		for (FrameworkMethod each : methods) {
			if (each.isStatic() && each.isPublic()) {
				return true;
			}
		}
		return false;
	}

	private FrameworkMethod getParametersMethod() throws Exception {
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
				Parameters.class);
		for (FrameworkMethod each : methods) {
			if (each.isStatic() && each.isPublic()) {
				return each;
			}
		}
		throw new Exception("No public static parameters method on class "
				+ getTestClass().getName());
	}

	private Exception parametersMethodReturnedWrongType() throws Exception {
		String className = getTestClass().getName();
		String methodName = getParametersMethod().getName();
		String message = MessageFormat.format(
				"{0}.{1}() must return an Iterable of arrays.",
				className, methodName);
		return new Exception(message);
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Parameters {
	}
}
