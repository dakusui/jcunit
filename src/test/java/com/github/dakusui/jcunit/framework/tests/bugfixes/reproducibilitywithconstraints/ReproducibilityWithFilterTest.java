package com.github.dakusui.jcunit.framework.tests.bugfixes.reproducibilitywithconstraints;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Filter;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class ReproducibilityWithFilterTest {
  @Rule
  public JCUnitDesc desc = new JCUnitDesc();

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

  @Filter
  public static boolean filter(Tuple tuple) {
    return tuple.get("a").equals(3);
  }


  @Test
  public void test() {
    String s = this.desc.getId() + ";" + TupleUtils.toString(this.desc.getTestCase());
    System.out.println(s);
    assertEquals(expectations.get(this.desc.getId()), s);
    expectations.remove(this.desc.getId());
  }

  @AfterClass
  public static void afterClass() {
    assertTrue(expectations.isEmpty());
  }
}
