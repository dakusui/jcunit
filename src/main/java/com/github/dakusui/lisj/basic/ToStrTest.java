package com.github.dakusui.lisj.basic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.lisj.Basic;

public class ToStrTest {
	@Test
	public void tostr_FQCNlikeString_true_1() {
		assertEquals(
				".com.github.hello.World#Test", 
				Basic.tostr(".com.github.hello.World#Test", true)
		);
	}

	@Test
	public void tostr_FQCNlikeString_true_2() {
		assertEquals(
				"com.github.hello.World#Test.", 
				Basic.tostr("com.github.hello.World#Test.", true)
		);
	}
	@Test
	public void tostr_FQCNlikeString_true_3() {
		assertEquals(
				"World#Test", 
				Basic.tostr("com.github.hello.World#Test", true)
		);
	}
	@Test
	public void tostr_FQCNlikeString_true_4() {
		assertEquals(
				"hello.World#Test", 
				Basic.tostr("hello.World#Test", true)
		);
	}
	@Test
	public void tostr_FQCNlikeString_false() {
		////
		/// Regardless of 'suppressObjectId' parameter, the string will be
		//  compacted as long as it looks an FQCN.
		assertEquals(
				"World#Test", 
				Basic.tostr("com.github.hello.World#Test", false)
		);
	}
}
