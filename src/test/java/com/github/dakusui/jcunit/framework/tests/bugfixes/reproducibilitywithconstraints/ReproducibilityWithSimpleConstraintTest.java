package com.github.dakusui.jcunit.framework.tests.bugfixes.reproducibilitywithconstraints;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Constraint;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.TupleGeneration;
import com.github.dakusui.jcunit.core.rules.JCUnitDesc;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;
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
@TupleGeneration(
		constraint = @Constraint(ReproducibilityWithSimpleConstraintTest.CM.class)
)
public class ReproducibilityWithSimpleConstraintTest {
	public static class CM extends ConstraintManagerBase {
		@Override
		public boolean check(Tuple tuple) throws JCUnitSymbolException {
			if (!tuple.containsKey("a") || !tuple.containsKey("b"))
				throw new JCUnitSymbolException();
			return !(new Integer(3).equals(tuple.get("a")) && new Integer(3).equals(tuple.get("b")));
		}
	}

	@Rule
	public JCUnitDesc desc = new JCUnitDesc();

	@FactorField(intLevels = {1, 2, 3})
	public int a;

	@FactorField(intLevels = {1, 2, 3})
	public int b;

	@FactorField(intLevels = {1, 2, 3})
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
		expectations.put(8, "8;{\"a\":2,\"b\":3,\"c\":3}");
		expectations.put(9, "9;{\"a\":3,\"b\":1,\"c\":3}");
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
