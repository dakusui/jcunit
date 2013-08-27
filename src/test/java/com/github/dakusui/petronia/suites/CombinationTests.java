package com.github.dakusui.petronia.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.github.dakusui.petronia.ct.JCUnitTest;
import com.github.dakusui.petronia.ct.WithNot;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	JCUnitTest.class,
	WithNot.class
})
public class CombinationTests {}
