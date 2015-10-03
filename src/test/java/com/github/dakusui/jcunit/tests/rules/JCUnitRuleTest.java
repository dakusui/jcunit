package com.github.dakusui.jcunit.tests.rules;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.rules.JCUnitRule;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JCUnitRuleTest extends JCUnitRule {
    @Mock
    public Description description;
    @Mock
    public JCUnit.InternalAnnotation ann;

    Class<?> testClass = JCUnitRuleTest.class;
    Factors factors = new Factors.Builder().add(new Factor.Builder().setName("f1").addLevel(1).build()).build();
    Tuple tuple = new Tuple.Builder().build();
    JCUnit.TestCaseType type = JCUnit.TestCaseType.Custom;


    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        when(description.getTestClass()).thenReturn((Class) testClass);
        when(description.getAnnotation(JCUnit.InternalAnnotation.class)).thenReturn(ann);
        when(description.getMethodName()).thenReturn("methodName");
        when(ann.getTestCase()).thenReturn(tuple);
        when(ann.getTestCaseType()).thenReturn(type);
        when(ann.getId()).thenReturn(123);
        when(ann.getFactors()).thenReturn(factors);
    }

    @Test
    public void testJCUnitRule() {
        starting(description);
        assertEquals(type, this.getTestCaseType());
        assertEquals(testClass, this.getTestClass());
        assertEquals("methodName", this.getTestName());
        assertEquals(tuple, this.getTestCase());
        assertEquals(123, this.getId());
        assertEquals(factors, this.getFactors());
    }

}
