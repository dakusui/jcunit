package com.github.dakusui.petronia.ut.forms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.dakusui.jcunit.auto.Auto;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Form;

public class AutoFunctionTest extends FormTestBase {
	public static class Example {
		@Out
		public int example = 100;
		public Example(int i) {
			this.example = i;
		}
	}

	@Rule
	public TestName testName = new TestName();
	
	@Before
	public void removeWorkingDirectory() throws IOException {
		FileUtils.deleteDirectory(new File(".jcunit"));
	}
	
	@Override
	protected Form createForm() {
		return new Auto();
	}
	
	@Test
	public void test1() throws JCUnitException, CUT {
		assertEquals(
				false, 
				eval(
					this.testName,
					new Example(100),
					"example"
				)
		);
	}

	@Test
	public void test2() throws JCUnitException, CUT {
		assertEquals(
				false, 
				eval(
					this.testName,
					new Example(100),
					"example"
				)
		);
		assertEquals(
				true, 
				eval(
					this.testName,
					new Example(100),
					"example"
				)
		);
	}

	@Test
	public void test3() throws JCUnitException, CUT {
		assertEquals(
				false, 
				eval(
					this.testName,
					new Example(100),
					"example"
				)
		);
		assertEquals(
				false, 
				eval(
					this.testName,
					new Example(101),
					"example"
				)
		);
	}
}
