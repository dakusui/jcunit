package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.compat.core.CompatUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

class JCUnitRunner extends BlockJUnit4ClassRunner {
	private final List<Object> fParameterList;

	private final int fParameterSetNumber;

	JCUnitRunner(Class<?> type, List<Object> parameterList, int i)
			throws InitializationError {
		super(type);
		fParameterList = parameterList;
		fParameterSetNumber = i;
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return childrenInvoker(notifier);
	}

	@Override
	public Object createTest() throws Exception {
		TestClass klazz = getTestClass();
		Object ret = klazz.getJavaClass().newInstance();
		Map<Field, Object> values = (Map<Field, Object>) computeParams();
		CompatUtils.initializeTestObject(ret, values);
		return ret;
	}

	@Override
	protected String getName() {
		return String.format("[%s]", fParameterSetNumber);
	}

	@Override
	protected String testName(final FrameworkMethod method) {
		return String.format("%s[%s]", method.getName(), fParameterSetNumber);
	}

	@Override
	protected void validateConstructor(List<Throwable> errors) {
		validateZeroArgConstructor(errors);
	}

	private Object computeParams() throws Exception {
		return fParameterList.get(fParameterSetNumber);
	}

}