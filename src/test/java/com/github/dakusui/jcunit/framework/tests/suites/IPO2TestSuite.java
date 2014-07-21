package com.github.dakusui.jcunit.framework.tests.suites;

import com.github.dakusui.jcunit.framework.tests.ipo2.SimpleConstraintConsciousTest;
import com.github.dakusui.jcunit.framework.tests.ipo2.Strength2Test;
import com.github.dakusui.jcunit.framework.tests.ipo2.Strength3Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    { Strength2Test.class, Strength3Test.class, SimpleConstraintConsciousTest.class})
public class IPO2TestSuite {
}
