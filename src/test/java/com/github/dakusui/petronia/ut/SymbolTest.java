package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.Symbol;

public class SymbolTest extends DefaultRuleSetBuilder {
	@Test
	public void symbol_01() throws Exception {
		Basic.eval(this, assign($("s1"), "Hello!"));

		Symbol s = $("s1");

		assertEquals("Hello!", Basic.eval(this, s));
	}
	
	@Test(expected=SymbolNotFoundException.class)
	public void symbol_02() throws Exception {
		Basic.eval(this, assign($("s1"), "Hello!"));

		Symbol s = $("s2");

		assertEquals("Hello!", Basic.eval(this, s));
	}
	
	@Test
	public void symbol_03() throws Exception {
		Symbol s10, s11, s2;
		Basic.eval(this, assign(s10 = $("s1"), "Hello!"));
		Basic.eval(this, assign(s11 = $("s1"), "Hello!"));
		Basic.eval(this, assign(s2 = $("s2"), "Hello!"));

		// The same thing must always be eq.
		assertEquals(true,  Basic.eq(s10, s10));
		// If the symbol names are the same, they are eq. 
		assertEquals(true,  Basic.eq(s10, s11));
		// But even if the assigned values are the same, they are not eq as long as their names are different.
		assertEquals(false, Basic.eq(s10, s2));
		assertEquals(false, Basic.eq(s11, s2));
	}	

	@Test
	public void symbol_04() throws Exception {
		Basic.eval(this, assign($("s10"), "hello!"));
		Basic.eval(this, assign($("s11"), "hello!"));
		Basic.eval(this, assign($("s2"), "HELLO!"));

		Symbol[] s = $("s10", "s11", "s2"); 
		// Even if the names are different, the same value can be retrieved.
		assertEquals("hello!", Basic.eval(this, s[0])); 
		assertEquals("hello!", Basic.eval(this, s[1])); 
		assertEquals("HELLO!", Basic.eval(this, s[2])); 
	}

	@Test
	public void symbol_05() throws Exception {
		Basic.eval(this, assign($("s1"), "hello!"));

		Symbol s = $("s1");
		// Even if the names are different, the same value can be retrieved.
		assertEquals("hello!!!", Basic.eval(this, format("%s!!", s))); 
	}
	
	@Test
	public void symbolEquality() throws Exception {
		Symbol s1 = $("s");
		Symbol s2 = $("s");
		
		Set<Symbol> symbolSet = new HashSet<Symbol>();
		symbolSet.add(s1);
		assertTrue(symbolSet.contains(s2));
		assertFalse(s1.equals(null));
		assertFalse(s1.equals(new Object()));
	}
}
