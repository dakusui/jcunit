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
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
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
        try {
            ////
            // Prepare filter method(s) and custom test case methods.
            List<String> frameworkMethodFailures = new LinkedList<String>();
            List<FrameworkMethod> preconditionMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodUtils.FrameworkMethodValidator.PRECONDITION);
            // Currently only one filter method can be used.
            // Custom test case methods.
            List<FrameworkMethod> customTestCaseMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodUtils.FrameworkMethodValidator.CUSTOM_TESTCASES);
            ////
            // Check if any error was found.
            ConfigUtils.checkEnv(frameworkMethodFailures.isEmpty(),
                    "Errors are found in test class '%s':%s",
                    getTestClass().getJavaClass().getCanonicalName(),
                    frameworkMethodFailures);

            ////
            // Generate a list of test cases using a specified tuple generator
            TupleGenerator tupleGenerator = TupleGeneratorFactory.INSTANCE
                    .createTupleGeneratorFromClass(klass);
            Factors factors = tupleGenerator.getFactors();
            int id;
            for (id = (int) tupleGenerator.firstId();
                 id >= 0; id = (int) tupleGenerator.nextId(id)) {
                Tuple testCase = tupleGenerator.get(id);
                if (shouldPerform(testCase, preconditionMethods)) {
                    runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
                            id, TestCaseType.Generated,
                            factors,
                            testCase));
                }
            }
            // Skip to number of test cases generated.
            id = (int) tupleGenerator.size();
            ////
            // Compose a list of 'negative test cases' and register them.
            ConstraintManager cm = tupleGenerator.getConstraintManager();
            final List<Tuple> violations = cm.getViolations();
            id = registerTestCases(
                    id,
                    factors,
                    violations,
                    TestCaseType.Violation,
                    preconditionMethods);
            ////
            // Compose a list of 'custom test cases' and register them.
            registerTestCases(
                    id,
                    factors,
                    invokeCustomTestCasesMethod(customTestCaseMethods),
                    TestCaseType.Custom,
                    preconditionMethods);
            ConfigUtils.checkEnv(runners.size() > 0, "No test to be run was found.");
        } catch (JCUnitUserException e) {
            e.setTargetClass(klass);
            throw e;
        }
    }

    static Object createTestObject(TestClass testClass, Tuple testCase) {
        return TestCaseUtils.toTestObject(testClass.getJavaClass(), testCase);
    }

    private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
        if (preconditionMethods.isEmpty()) return true;
        for (FrameworkMethod m : preconditionMethods) {
            try {
                Object testObject = createTestObject(this.getTestClass(),
                    testCase);
                if ((Boolean) m.invokeExplosively(null, testObject)) return true;
            } catch (Throwable throwable) {
                ConfigUtils.rethrow(throwable, "Failed to execute ");
            }
        }
        return false;
    }

    private int registerTestCases(int id,
                                  Factors factors,
                                  Iterable<Tuple> testCases,
                                  TestCaseType testCaseType,
                                  List<FrameworkMethod> preconditionMethods)
            throws Throwable {
        for (Tuple testCase : testCases) {
            if (shouldPerform(testCase, preconditionMethods)) {
                runners.add(new JCUnitRunner(
                        getTestClass().getJavaClass(),
                        id,
                        testCaseType,
                        factors,
                        testCase));
            }
            id++;
        }
        return id;
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    private List<Tuple> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
        List<Tuple> ret = new LinkedList<Tuple>();
        try {
            for (FrameworkMethod m : customTestCasesMethods) {
                Object r = m.invokeExplosively(null);

                if (r instanceof Tuple) {
                    ret.add((Tuple) r);
                } else if (r instanceof Iterable) {
                    for (Object o : (Iterable) r) {
                        if (o == null) {
                            ConfigUtils.checkEnv(false, "Returned value of '%s' must not contain null.", m.getName());
                        }
                        if (o instanceof Tuple) {
                            ret.add((Tuple) o);
                        } else if (getTestClass().getJavaClass().isAssignableFrom(o.getClass())) {
                            ret.add(TestCaseUtils.toTestCase(o));
                        } else {
                            ConfigUtils.checkEnv(false, "Returned value of '%s' must contain only Tuple or test objects.", m.getName());
                        }
                    }
                } else {
                    Utils.checkcond(false);
                }
            }
        } catch (Throwable throwable) {
            Utils.rethrow(throwable, "Failed to execute '%s'.: (%s)", throwable.getMessage());
        }
        return ret;
    }

    private List<FrameworkMethod> getFrameworkMethods(List<String> failures, FrameworkMethodUtils.FrameworkMethodValidator validator) {
        List<FrameworkMethod> methods = validator.getMethods(getTestClass());
        List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methods.size());
        for (FrameworkMethod m : methods) {
            if (validator.validate(getTestClass().getJavaClass(), m, failures)) {
                ret.add(m);
            }
        }
        return ret;
    }


    /**
     * Identifies what kind of category to which a test case belongs.
     */
    public static enum TestCaseType {
        /**
         * A custom test case, which is returned by a method annotated with {@literal @}{@code CustomTestCases}.
         */
        Custom,
        /**
         * A generated test case. A test case generated by JCUnit framework through an implementation of {@code TupleGenerator}
         * belongs to this category.
         */
        Generated,
        /**
         * A test case which violates some defined constraint belongs to this category.
         * Test cases returned by {@code ConstraintManager#getViolations} belongs to this.
         */
        Violation
    }

    public static class InternalAnnotation implements Annotation {

        private final TestCaseType type;
        private final int id;
        private Factors factors;
        private Tuple testCase;

        public InternalAnnotation(TestCaseType type, int id, Factors factors,
                                  Tuple testCase) {
            Utils.checknotnull(type);
            this.id = id;
            this.type = type;
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

        public Tuple getTestCase() {
            return testCase;
        }

        public Factors getFactors() {
            return factors;
        }
    }
}
