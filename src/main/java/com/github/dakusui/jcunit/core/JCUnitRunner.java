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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class JCUnitRunner extends BlockJUnit4ClassRunner {
    private final Tuple testCase;
    private final int id;
    private final JCUnit.TestCaseType type;
    private final Factors factors;

    JCUnitRunner(Class<?> clazz, int id, JCUnit.TestCaseType testType,
                 Factors factors, Tuple testCase)
            throws InitializationError {
        super(clazz);
        Utils.checknotnull(testCase);
        this.factors = factors;
        this.testCase = testCase;
        this.id = id;
        this.type = testType;
    }

    /**
     * Returns {@code null}, if the list {@code whenMethods} is empty, which means
     * all test cases should be executed.
     * <p/>
     * An empty set returned by this method means no test method should be executed
     * for the given {@code testCase}.
     */
    private static boolean shouldInvoke(Object testObject,
                                        List<FrameworkMethod> preconditions) {
        if (preconditions == null) {
            return true;
        }
        List<String> failures = new LinkedList<String>();
        for (FrameworkMethod each : preconditions) {
            try {
                if ((Boolean) each.invokeExplosively(null, testObject)) {
                    return true;
                }
            } catch (Throwable throwable) {
                ConfigUtils.rethrow(throwable,
                        "Failed to invoke test precondition method '%s'(%s)",
                        each.getName(),
                        each.getType().getDeclaringClass().getCanonicalName()
                );
            }
        }
        Utils.checkcond(failures.isEmpty(), "Some errors are detected.: %s",
                failures);
        return false;
    }

    /**
     * Without overriding this method, all the tests will fail for 'AssertionError',
     * because {@code {@literal @}BeforeClass} methods and {@code {@literal @}AfterClass}
     * methods are executed for every test case run not before and after all the
     * test cases are executed.
     * <p/>
     * {@code BlockJUnit4ClassRunnerWithParameters} does the same.
     *
     * @see org.junit.runners.BlockJUnit4ClassRunner#classBlock(org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return childrenInvoker(notifier);
    }

    /**
     * Overrides super class's {@code createTestObject()} method, which throws a {@code java.lang.Exception},
     * to simplify exception handling.
     */
    @Override
    public Object createTest() {
        TestClass klazz = getTestClass();
        return JCUnit.createTestObject(klazz, testCase);
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
        ArrayList<Annotation> annotations = new ArrayList<Annotation>(
                work.length + 1);
        annotations.add(
                new JCUnit.InternalAnnotation(this.type, this.id, this.factors,
                        this.testCase));
        Collections.addAll(annotations, work);
        return Description.createTestDescription(getTestClass().getJavaClass(),
                testName(method),
                annotations.toArray(new Annotation[annotations.size()]));
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
        for (FrameworkMethod each : this.computeTestMethods()) {
            assert this.testCase != null;
            if (this.shouldInvoke(each, createTest())) {
                ret.add(each);
            }
        }
        if (ret.isEmpty()) {
            throw new RuntimeException(String
                    .format("No matching test method is found for test: %s",
                            this.testCase));
        }
        return ret;
    }

    private boolean shouldInvoke(FrameworkMethod testMethod, Object testObject) {
        List<String> failures = new LinkedList<String>();
        List<FrameworkMethod> preconditionMethods = FrameworkUtils.getTestPreconditionMethodsFor(
            getTestClass().getJavaClass(),
            testMethod, failures);
        ConfigUtils.checkTest(failures.isEmpty(),
                "Errors are found while precondition checks.: %s", failures);
        return shouldInvoke(testObject, preconditionMethods);
    }

}