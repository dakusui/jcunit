package com.github.dakusui.petronia.suites;

import com.github.dakusui.petronia.ct.JCUnitTest;
import com.github.dakusui.petronia.ct.WithNot;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ JCUnitTest.class, WithNot.class })
public class CombinationTests {
}
