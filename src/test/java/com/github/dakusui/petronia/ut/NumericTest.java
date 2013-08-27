package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;

public class NumericTest extends DefaultRuleSetBuilder {
	@Test public void add_00() throws Exception {
		assertEquals(Utils.bigDecimal(0), Utils.bigDecimal((Number) Basic.eval(this, add())));
	}
	@Test public void add_01() throws Exception {
		assertEquals(Utils.bigDecimal(1), Utils.bigDecimal((Number) Basic.eval(this, add(1))));
	}
	@Test public void add_02() throws Exception {
		assertEquals(Utils.bigDecimal(3), Utils.bigDecimal((Number) Basic.eval(this, add(1, 2))));
	}
	@Test public void add_03() throws Exception {
		assertEquals(Utils.bigDecimal(6), Utils.bigDecimal((Number) Basic.eval(this, add(1, 2, 3))));
	}

	@Test(expected=IllegalArgumentException.class) public void sub_00() throws Exception {
		Utils.bigDecimal((Number) Basic.eval(this, sub()));
	}
	
	@Test public void sub_01() throws Exception {
		assertEquals(Utils.bigDecimal(1), Utils.bigDecimal((Number) Basic.eval(this, sub(1))));
	}
	@Test public void sub_02() throws Exception {
		assertEquals(Utils.bigDecimal(-1), Utils.bigDecimal((Number) Basic.eval(this, sub(1, 2))));
	}
	@Test public void sub_03() throws Exception {
		assertEquals(Utils.bigDecimal(-4), Utils.bigDecimal((Number) Basic.eval(this, sub(1, 2, 3))));
	}

	@Test public void mul_00() throws Exception {
		assertEquals(Utils.bigDecimal(1), Utils.bigDecimal((Number) Basic.eval(this, mul())));
	}
	@Test public void mul_01() throws Exception {
		assertEquals(Utils.bigDecimal(1), Utils.bigDecimal((Number) Basic.eval(this, mul(1))));
	}
	@Test public void mul_02() throws Exception {
		assertEquals(Utils.bigDecimal(2), Utils.bigDecimal((Number) Basic.eval(this, mul(1, 2))));
	}
	@Test public void mul_03() throws Exception {
		assertEquals(Utils.bigDecimal(6), Utils.bigDecimal((Number) Basic.eval(this, mul(1, 2, 3))));
	}

	@Test public void div_00() throws Exception {
		boolean passed = false;
		try {
			Utils.bigDecimal((Number) Basic.eval(this, div()));
		} catch (IllegalArgumentException e) {
			passed = true;
		}
		assertTrue(passed);
	}
	@Test public void div_01() throws Exception {
		assertEquals(Utils.bigDecimal(1), Utils.bigDecimal((Number) Basic.eval(this, div(1))));
	}
	@Test public void div_02a() throws Exception {
		assertEquals(Utils.bigDecimal(0.5), Utils.bigDecimal((Number) Basic.eval(this, div(1, 2))));
	}
	@Test public void div_02b() throws Exception {
		assertEquals("0.3333333333333333333333333333333333", Utils.bigDecimal((Number) Basic.eval(this, div(1, 3))).toString());
	}
	@Test public void div_03() throws Exception {
		assertEquals(Utils.bigDecimal(0.25), Utils.bigDecimal((Number) Basic.eval(this, div(1, 2, 2))));
	}
}