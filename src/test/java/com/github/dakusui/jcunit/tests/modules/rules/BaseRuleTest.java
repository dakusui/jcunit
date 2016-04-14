package com.github.dakusui.jcunit.tests.modules.rules;

import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.runners.standard.InternalAnnotation;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.rules.BaseRule;
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
public class BaseRuleTest extends BaseRule {
  @Mock
  public Description        description;
  @Mock
  public InternalAnnotation ann;

  Class<?>      testClass = BaseRuleTest.class;
  Factors       factors   = new Factors.Builder().add(new Factor.Builder("f1").addLevel(1).build()).build();
  Tuple         tuple     = new Tuple.Builder().build();
  TestCase.Type type      = TestCase.Type.CUSTOM;
  TestCase      testCase  = new JCUnit.NumberedTestCase(123, this.type, this.tuple);


  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    when(description.getTestClass()).thenReturn((Class) testClass);
    when(description.getAnnotation(InternalAnnotation.class)).thenReturn(ann);
    when(description.getMethodName()).thenReturn("methodName");
    when(ann.getTestCase()).thenReturn((JCUnit.NumberedTestCase) testCase);
    when(ann.getFactors()).thenReturn(factors);
  }

  @Test
  public void testJCUnitRule() {
    starting(description);
    assertEquals(type, this.getTestCase().getType());
    assertEquals(testClass, this.getTestClass());
    assertEquals("methodName", this.getTestName());
    assertEquals(tuple, this.getTestCase().getTuple());
    assertEquals(123, this.getTestCase().getId());
    assertEquals(factors, this.getFactors());
  }

}
