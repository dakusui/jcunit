package com.github.dakusui.jcunit.framework.tests.suites;

import com.github.dakusui.jcunit.framework.tests.ipo2.Strength2;
import com.github.dakusui.jcunit.framework.tests.ipo2.Strength3;
import com.github.dakusui.jcunit.framework.utils.tuples.SimpleConstraintConsciousTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({Strength2.class, Strength3.class, SimpleConstraintConsciousTest.class})
public class IPO2TestSuite {
}
