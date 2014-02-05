package com.github.dakusui.petronia.ut.forms;

import org.junit.Test;

import com.github.dakusui.jcunit.auto.OutFieldNames;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Form;

public class OutFieldNamesTest extends FormTestBase {
	@Out
	public String test2;

	@Override
	protected Form createForm() {
		return new OutFieldNames();
	}

	@Test
	public void test1() throws JCUnitException, CUT {
		System.out.println(eval(this));
	}
}
