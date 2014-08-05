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
import java.lang.reflect.Method;
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
            for (id = (int) tupleGenerator.firstId();
                 id >= 0; id = (int) tupleGenerator.nextId(id)) {
                runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
                        id, TestCaseType.Generated, new LinkedList<Serializable>(),
                        factors,
                        tupleGenerator.get(id)));
            }
            id = (int) tupleGenerator.size();
            ConstraintManager cm = tupleGenerator.getConstraintManager();
            final List<LabeledTestCase> violations = cm.getViolations();
            id = registerLabeledTestCases(
                    id,
                    factors,
                    violations,
                    TestCaseType.Custom);
            if (hasCustomTestCasesMethod()) {
                registerLabeledTestCases(
                        id,
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
                                         Factors factors,
                                         Iterable<LabeledTestCase> labeledTestCases,
                                         TestCaseType testCaseType)
            throws InitializationError {
        for (LabeledTestCase labeledTestCase : labeledTestCases) {
            runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
                    id, testCaseType, labeledTestCase.getLabels(),
                    factors,
                    labeledTestCase.getTestCase()));
            id++;
        }
        return id;
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

    private boolean hasFilterMethod() {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
                Filter.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return true;
            }
        }
        return false;
    }


    private boolean hasCustomTestCasesMethod() {
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

    public static interface FrameworkMethodValidator {
        public static final FrameworkMethodValidator CUSTOM_TESTCASES = new FrameworkMethodValidator() {
            @Override
            public boolean validate(FrameworkMethod m) {
                Method mm = m.getMethod();
                return m.isPublic() && m.isStatic() && mm.getParameterTypes().length == 0 &&
                        (LabeledTestCase.class.isAssignableFrom(mm.getReturnType()) ||
                                Collections.class.isAssignableFrom(mm.getReturnType()));
            }

            @Override
            public Class<? extends Annotation> getAnnotation() {
                return CustomTestCases.class;
            }

            @Override
            public String getDescription() {
                return "public, static, no parameter, and returning 'LabeledTestCase' or an iterable of it";
            }
        };
        public static final FrameworkMethodValidator TESTCASE_FILTER = new FrameworkMethodValidator() {
            @Override
            public boolean validate(FrameworkMethod m) {
                Method mm = m.getMethod();
                boolean ret = Boolean.class.isAssignableFrom(mm.getReturnType());

                return m.isStatic() && m.isPublic() && ret;
            }

            @Override
            public Class<? extends Annotation> getAnnotation() {
                return Filter.class;
            }

            @Override
            public String getDescription() {
                return "public, static, returning boolean, parameters are ";
            }
        };

        public boolean validate(FrameworkMethod m);

        public Class<? extends Annotation> getAnnotation();

        public String getDescription();
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
}
