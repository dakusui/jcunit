package com.github.dakusui.suites;

import com.github.dakusui.jcunit.extras.examples.Example;
import com.github.dakusui.jcunit.extras.examples.MethodFinderTest;
import com.github.dakusui.jcunit.extras.examples.TestArrayGeneratorsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ Example.class, MethodFinderTest.MethodFinderTest1.class,
    MethodFinderTest.MethodFinderTest2.class, TestArrayGeneratorsTest.class })
public class ExampleSuite {
}
