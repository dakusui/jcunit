package com.github.dakusui.suites;

import com.github.dakusui.jcunit.extras.ct.JCUnitTest;
import com.github.dakusui.lisj.forms.WithNot;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ JCUnitTest.class, WithNot.class })
public class CombinationTests {
}
