package com.github.dakusui.petronia.suites;

import com.github.dakusui.lisj.basic.ToStrTest;
import com.github.dakusui.petronia.ut.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
/*
 * Tests for functions and predicates.
 */
    AlwaysTrueTest.class, AssignTest.class,
    CartesianTestArrayGeneratorTest.class,
    CompTest.class, ConcatTest.class, CondTest.class, ConsTest.class,
    ConsTest2.class, EqTest.class, EvalTest.class, FormatTest.class,
    FormEvalTest.class, IsInstanceOfTest.class, IsOneOfTest.class,
    IPOTest.class, Java.class, LambdaTest.class, LogicalPredicateTest.class,
    LoopTest.class, MaxMinTest.class, NumCastTest.class, NumericFuncTest.class,
    NumericTest.class, PairwiseTestArrayGeneratorTest.class, PrognTest.class,
    QuoteTest.class, RuleSetTest.class, SimpleTestArrayGeneratorTest.class,
    SymbolTest.class, WhenTest.class,
    /*
     * Tests for utilities
     */
    StrTest.class, ToStrTest.class, UtilsTest.class })
public class UnitTests {
}
