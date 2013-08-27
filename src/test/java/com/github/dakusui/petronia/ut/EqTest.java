package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class EqTest extends DefaultRuleSetBuilder {
	@Test
	public void eq_t01() throws JCUnitException, CUT {
		assertEquals(true, Basic.eval(this, eq(1, 1)));
	}
	@Test
	public void eq_t02() throws JCUnitException, CUT {
		assertEquals(true, Basic.eval(this, eq(null, null)));
	}
	@Test
	public void eq_t03() throws JCUnitException, CUT {
		assertEquals(true, Basic.eval(this, eq(new Object[0], Basic.NIL)));
	}
	@Test
	public void eq_t04() throws JCUnitException, CUT {
		assertEquals(true, Basic.eval(this, eq(new Object[0], new Object[0])));
	}
	@Test
	public void eq_f01() throws JCUnitException, CUT {
		assertEquals(false, Basic.eval(this, eq(1, 2)));
	}
	@Test
	public void eq_f02() throws JCUnitException, CUT {
		assertEquals(false, Basic.eval(this, eq(1, null)));
	}
	@Test
	public void eq_f03() throws JCUnitException, CUT {
		assertEquals(false, Basic.eval(this, eq(null, 1)));
	}
}