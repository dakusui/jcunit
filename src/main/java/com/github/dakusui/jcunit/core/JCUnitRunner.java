package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.*;

class JCUnitRunner extends BlockJUnit4ClassRunner {
    private final Tuple testCase;
    private final int id;
    private final JCUnit.TestCaseType type;
    private final Factors factors;
    private final Map<String, FrameworkMethod> methods = new HashMap<String, FrameworkMethod>();

    /**
     * Creates an object of this class.
     *
     * @param clazz    A test class.
     * @param id       An id of the test case to be run.
     * @param testType Test case type.
     * @param factors  A factors object which defines the domain of the test cases.
     * @param testCase A test case itself.
     * @throws InitializationError In case initialization is failed. e.g. More than one constructor is found in the test class.
     */
    JCUnitRunner(Class<?> clazz, int id, JCUnit.TestCaseType testType,
                 Factors factors, Tuple testCase)
            throws InitializationError {
        super(clazz);
        Checks.checknotnull(testCase);
        this.factors = factors;
        this.testCase = testCase;
        this.id = id;
        this.type = testType;
        List<String> errors = new LinkedList<String>();
        for (FrameworkMethod each : FrameworkMethodUtils.FrameworkMethodRetriever.REFERENCED_BY_GIVEN.getMethods(clazz)) {
            FrameworkMethodUtils.validateFrameworkMethod(
                    clazz,
                    each,
                    FrameworkMethodUtils.FrameworkMethodValidator.VALIDATOR_FOR_METHOD_REFERENCEDBY_GIVEN,
                    errors
            );
            this.methods.put(each.getName(), each);
        }
        if (!errors.isEmpty()) {
            throw new InitializationError(String.format("Errors are found in '%s'.: %s", clazz.getCanonicalName(), errors));
        }
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
        Checks.checknotnull(method);

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
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            runLeaf(methodBlock(method), description, notifier);
        }
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
        for (FrameworkMethod each : computeTestMethods()) {
            if (shouldInvoke(each, createTest())) ret.add(each);
        }
        if (ret.isEmpty())
            ret.add(getDummyMethodForNoMatchingMethodFound());
        return ret;
    }

    private boolean shouldInvoke(FrameworkMethod testMethod, Object testObject) {
        Given given = testMethod.getAnnotation(Given.class);
        if (given == null) return true;
        String preconditionMethodName = FrameworkMethodUtils.getPreconditionMethodNameFor(given);
        FrameworkMethod preconditionMethod = this.methods.get(preconditionMethodName);
        Checks.checkcond(preconditionMethod != null, "Something went wrong: name=%s, methdos=%s", preconditionMethodName, this.methods);
        assert preconditionMethod != null;
        boolean ret = false;
        try {
            ////
            // It's guaranteed that preconditionMethod returns a boolean by validation process.
            ret = (Boolean) preconditionMethod.invokeExplosively(testObject);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable throwable) {
            Checks.rethrow(throwable);
        }
        return ret;
    }

    private static FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
        try {
            return new FrameworkMethod(JCUnitRunner.class.getMethod("noMatchingTestMethodIsFoundForThisTestCase"));
        } catch (NoSuchMethodException e) {
            assert false;
        }
        Checks.checkcond(false);
        return null;
    }

    /**
     * This method is only used through reflection to let JUnit know the test case is ignored since
     * no matching test method is defined for it.
     *
     * @see JCUnitRunner#getDummyMethodForNoMatchingMethodFound()
     */
    @Ignore
    @SuppressWarnings("unused")
    public static void noMatchingTestMethodIsFoundForThisTestCase() {
    }
}