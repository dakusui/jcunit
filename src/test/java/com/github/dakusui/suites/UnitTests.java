package com.github.dakusui.suites;

import com.github.dakusui.jcunit.extras.generators.*;
import com.github.dakusui.lisj.basic.ToStrTest;
import com.github.dakusui.lisj.forms.control.*;
import com.github.dakusui.lisj.forms.numeric.NumCastTest;
import com.github.dakusui.lisj.forms.predicates.StrTest;
import com.github.dakusui.lisj.forms.predicates.comparison.CompTest;
import com.github.dakusui.lisj.forms.predicates.comparison.EqTest;
import com.github.dakusui.lisj.forms.predicates.logical.LogicalPredicateTest;
import com.github.dakusui.lisj.forms.numeric.MaxMinTest;
import com.github.dakusui.lisj.forms.numeric.NumericFuncTest;
import com.github.dakusui.lisj.forms.numeric.NumericTest;
import com.github.dakusui.lisj.forms.predicates.AlwaysTrueTest;
import com.github.dakusui.lisj.forms.predicates.IsInstanceOfTest;
import com.github.dakusui.lisj.forms.predicates.IsOneOfTest;
import com.github.dakusui.lisj.forms.str.*;
import com.github.dakusui.jcunit.extras.ut.*;
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
    Cons2Test.class, EqTest.class, EvalTest.class, FormatTest.class,
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
