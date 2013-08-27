package com.github.dakusui.petronia.examples;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestArrayGeneratorsTest {
	@Test public void simpleTestArrayGenerator() throws Exception {
		Result result = JUnitCore.runClasses(CalcTest.class);
		assertEquals(0, result.getFailureCount());
	}
	
	@Test public void cartesianTestArrayGenerator() throws Exception {
		Result result = JUnitCore.runClasses(CalcTest2.class);
		assertEquals(245,    result.getRunCount());
		assertEquals(43 + 7, result.getFailureCount());
	}
	
	@Test public void pairwiseTestArrayGenenrator() throws Exception {
		Result result = JUnitCore.runClasses(CalcTest3.class);
		for (Failure f: result.getFailures()) {
			System.out.println(f);
		}
		assertEquals(119,    result.getRunCount());
		assertEquals( 10 + 1,result.getFailureCount());
	}
}
