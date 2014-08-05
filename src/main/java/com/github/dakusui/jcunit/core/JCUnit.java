package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitUserException;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
		try {
			TupleGenerator tupleGenerator = TupleGeneratorFactory.INSTANCE
					.createTupleGeneratorFromClass(klass);
			Factors factors = tupleGenerator.getFactors();
			int id;
			TestCaseFilter filter = createTestCaseFilter(klass);
			for (id = (int) tupleGenerator.firstId();
			     id >= 0; id = (int) tupleGenerator.nextId(id)) {
				if (filter.shouldBeExecuted(id)) {
					runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
							id, TestCaseType.Generated, new LinkedList<Serializable>(),
							factors,
							tupleGenerator.get(id)));
				}
			}
			id = (int) tupleGenerator.size();
			ConstraintManager cm = tupleGenerator.getConstraintManager();
			final List<LabeledTestCase> violations = cm.getViolations();
			id = registerLabeledTestCases(
					id,
					filter,
					factors,
					violations,
					TestCaseType.Custom);
			if (hasParametersMethod()) {
				registerLabeledTestCases(
						id,
						filter,
						factors,
						allCustomTestCases(),
						TestCaseType.Violation);
			}
			ConfigUtils.checkEnv(runners.size() > 0, "No test to be run was found.");
		} catch (JCUnitUserException e) {
			e.setTargetClass(klass);
			throw e;
		}
	}

	private int registerLabeledTestCases(int id,
	                                     TestCaseFilter filter,
	                                     Factors factors,
	                                     Iterable<LabeledTestCase> labeledTestCases,
	                                     TestCaseType testCaseType)
			throws InitializationError {
		for (LabeledTestCase labeledTestCase : labeledTestCases) {
			if (filter.shouldBeExecuted(id)) {
				runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
						id, testCaseType, labeledTestCase.getLabels(),
						factors,
						labeledTestCase.getTestCase()));
			}
			id++;
		}
		return id;
	}

	private TestCaseFilter createTestCaseFilter(Class<?> klass) {
		if (klass.isAnnotationPresent(Execute.class)) {
			return TestCaseFilter
					.createTestCaseFilter(klass.getAnnotation(Execute.class));
		} else {
			return TestCaseFilter.createTestCaseFilter();
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
				CustomTestCases.class);
		for (FrameworkMethod each : methods) {
			if (each.isStatic() && each.isPublic()) {
				return true;
			}
		}
		return false;
	}

	private FrameworkMethod getParametersMethod() throws Exception {
		List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
				CustomTestCases.class);
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

	static class TestCaseFilter {
		private final int[] include;
		private final int[] exclude;

		TestCaseFilter(int[] include, int[] exclude) {
			if (include != null) {
				for (int i : include) {
					Utils.checkcond(i >= 0,
							"include mustn't contain negative index. (%d was found)", i);
				}
				this.include = Arrays.copyOf(include, include.length);
				Arrays.sort(this.include);
			} else {
				this.include = null;
			}
			Utils.checknotnull(exclude);
			for (int i : exclude) {
				Utils.checkcond(i >= 0,
						"exclude mustn't contain negative index. (%d was found)", i);
			}
			this.exclude = Arrays.copyOf(exclude, exclude.length);
			Arrays.sort(this.exclude);
		}

		boolean shouldBeExecuted(int testCaseIndex) {
			if (this.include == null) {
				return Arrays.binarySearch(this.exclude, testCaseIndex) < 0;
			}
			return Arrays.binarySearch(this.include, testCaseIndex) >= 0
					&& Arrays.binarySearch(this.exclude, testCaseIndex) < 0;
		}

		public static TestCaseFilter createTestCaseFilter(Execute annotation) {
			int[] included = annotation.include();
			if (included.length == 1) {
				if (included[0] == -1) {
					included = null;
				}
			}
			int[] excluded = annotation.exclude();
			return new TestCaseFilter(included, excluded);
		}

		public static TestCaseFilter createTestCaseFilter() {
			return new TestCaseFilter(null, new int[]{});
		}

	}

	public static class TestCaseInternalAnnotation implements Annotation {

		private final TestCaseType type;
		private final List<Serializable> labels;
		private final int id;
		private Factors factors;
		private Tuple testCase;

		public TestCaseInternalAnnotation(int id, TestCaseType type,
		                                  List<Serializable> labels, Factors factors, Tuple testCase) {
			Utils.checknotnull(type);
			Utils.checknotnull(labels);
			this.id = id;
			this.type = type;
			this.labels = labels;
			this.factors = factors;
			this.testCase = testCase;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return this.getClass();
		}

		public int getId() {
			return this.id;
		}

		public TestCaseType getTestCaseType() {
			return this.type;
		}

		public List<Serializable> getLabels() {
			return Collections.unmodifiableList(this.labels);
		}

		public Tuple getTestCase() {
			return testCase;
		}

		public Factors getFactors() {
			return factors;
		}
	}

	public static enum TestCaseType {
		Custom,
		Generated,
		Violation
	}

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Execute {
		/**
		 * If '-1' is the only element of this attribute, all the test cases will
		 * be executed.
		 */
		public int[] include() default {-1};

		public int[] exclude() default {};
	}
}
