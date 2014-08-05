package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
	private final Tuple testCase;
	private final int id;
	private final JCUnit.TestCaseType type;
	private final List<Serializable> labels;
	private final Factors factors;

	JCUnitRunner(Class<?> clazz, int id, JCUnit.TestCaseType testType,
	             List<Serializable> labels, Factors factors, Tuple testCase)
			throws InitializationError {
		super(clazz);
		Utils.checknotnull(testCase);
		this.factors = factors;
		this.testCase = testCase;
		this.id = id;
		this.type = testType;
		this.labels = Collections.unmodifiableList(labels);
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return childrenInvoker(notifier);
	}

	@Override
	public Object createTest() throws Exception {
		TestClass klazz = getTestClass();
		Object ret = klazz.getJavaClass().newInstance();
		Utils.initializeObjectWithSchemafulTuple(ret, testCase);
		return ret;
	}

	@Override
	protected String getName() {
		return String.format("[%d]", this.id);
	}

	@Override
	protected String testName(final FrameworkMethod method) {
		return String.format("%s[%d]", method.getName(), this.id);
	}

	@Override
	protected void validateConstructor(List<Throwable> errors) {
		validateZeroArgConstructor(errors);
	}


	@Override
	protected Description describeChild(FrameworkMethod method) {
		Utils.checknotnull(method);

		Annotation[] work = method.getAnnotations();
		ArrayList<Annotation> annotations = new ArrayList<Annotation>(work.length + 1);
		annotations.add(new JCUnit.TestCaseInternalAnnotation(this.id, this.type, this.labels, this.factors, this.testCase));
		Collections.addAll(annotations, work);
		return Description.createTestDescription(getTestClass().getJavaClass(),
				testName(method), annotations.toArray(new Annotation[annotations.size()]));
	}
}