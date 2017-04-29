package com.github.dakusui.jcunit.tests.bugfixes.reproducibilitywithconstraints;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.Precondition;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class ReproducibilityWithPreconditionTest {
  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @Rule
  public TestDescription desc = new TestDescription();

  @FactorField(intLevels = { 1, 2, 3 })
  public int a;

  @FactorField(intLevels = { 1, 2, 3 })
  public int b;

  @FactorField(intLevels = { 1, 2, 3 })
  public int c;

  static Map<Integer, String> expectations = new HashMap<Integer, String>();

  @BeforeClass
  public static void beforeClass() {
    expectations.put(6, "6;{\"a\":3,\"b\":1,\"c\":2}");
    expectations.put(7, "7;{\"a\":3,\"b\":2,\"c\":1}");
    expectations.put(8, "8;{\"a\":3,\"b\":3,\"c\":3}");
  }

  @Precondition
  public boolean filter() {
    return this.a == 3;
  }


  @Test
  public void test() {
    String s = this.desc.getTestCase().getId() + ";" + TupleUtils.toString(this.desc.getTestCase().getTuple());
    UTUtils.stdout().println(s);
    assertEquals(expectations.get(this.desc.getTestCase().getId()), s);
    expectations.remove(this.desc.getTestCase().getId());
  }

  @Test
  public void test2() {

  }

  @AfterClass
  public static void afterClass() {
    assertTrue(expectations.isEmpty());
  }
}
