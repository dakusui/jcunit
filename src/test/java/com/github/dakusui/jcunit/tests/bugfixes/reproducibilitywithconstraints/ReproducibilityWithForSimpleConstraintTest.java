package com.github.dakusui.jcunit.tests.bugfixes.reproducibilitywithconstraints;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.*;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
@TupleGeneration(
    constraint = @Constraint(ReproducibilityWithForSimpleConstraintTest.CM.class)
)
public class ReproducibilityWithForSimpleConstraintTest {
  public static class CM extends ConstraintManagerBase {
    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      Checks.checksymbols(tuple, "a", "b");
      return !(new Integer(3).equals(tuple.get("a")) && new Integer(3).equals(tuple.get("b")));
    }
  }

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

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
    expectations.put(0, "0;{\"a\":1,\"b\":1,\"c\":3}");
    expectations.put(1, "1;{\"a\":1,\"b\":2,\"c\":2}");
    expectations.put(2, "2;{\"a\":1,\"b\":3,\"c\":1}");
    expectations.put(3, "3;{\"a\":2,\"b\":1,\"c\":1}");
    expectations.put(4, "4;{\"a\":2,\"b\":2,\"c\":3}");
    expectations.put(5, "5;{\"a\":2,\"b\":3,\"c\":2}");
    expectations.put(6, "6;{\"a\":3,\"b\":1,\"c\":2}");
    expectations.put(7, "7;{\"a\":3,\"b\":2,\"c\":1}");
    expectations.put(8, "8;{\"a\":3,\"b\":2,\"c\":3}");
    expectations.put(9, "9;{\"a\":1,\"b\":3,\"c\":3}");
  }

  @Test
  public void test() {
    String s = this.desc.getId() + ";" + TupleUtils.toString(this.desc.getTestCase());
    UTUtils.stdout().println(s);
    assertEquals(expectations.get(this.desc.getId()), s);
    expectations.remove(this.desc.getId());
  }

  @AfterClass
  public static void afterClass() {
    assertTrue(expectations.isEmpty());
  }
}
