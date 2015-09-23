package com.github.dakusui.jcunit.tests.suites;

import com.github.dakusui.jcunit.tests.ipo2.ForSimpleConstraintConsciousTest;
import com.github.dakusui.jcunit.tests.ipo2.Strength2Test;
import com.github.dakusui.jcunit.tests.ipo2.Strength3Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    { Strength2Test.class, Strength3Test.class, ForSimpleConstraintConsciousTest.class})
public class IPO2TestSuite {
}
