package com.github.dakusui.petronia.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.github.dakusui.petronia.ut.AlwaysTrueTest;
import com.github.dakusui.petronia.ut.AssignTest;
import com.github.dakusui.petronia.ut.CartesianTestArrayGeneratorTest;
import com.github.dakusui.petronia.ut.CompTest;
import com.github.dakusui.petronia.ut.ConcatTest;
import com.github.dakusui.petronia.ut.CondTest;
import com.github.dakusui.petronia.ut.ConsTest;
import com.github.dakusui.petronia.ut.ConsTest2;
import com.github.dakusui.petronia.ut.EqTest;
import com.github.dakusui.petronia.ut.EvalTest;
import com.github.dakusui.petronia.ut.FormEvalTest;
import com.github.dakusui.petronia.ut.FormatTest;
import com.github.dakusui.petronia.ut.IPOTest;
import com.github.dakusui.petronia.ut.IsInstanceOfTest;
import com.github.dakusui.petronia.ut.IsOneOfTest;
import com.github.dakusui.petronia.ut.Java;
import com.github.dakusui.petronia.ut.LambdaTest;
import com.github.dakusui.petronia.ut.LogicalPredicateTest;
import com.github.dakusui.petronia.ut.LoopTest;
import com.github.dakusui.petronia.ut.MaxMinTest;
import com.github.dakusui.petronia.ut.NumCastTest;
import com.github.dakusui.petronia.ut.NumericFuncTest;
import com.github.dakusui.petronia.ut.NumericTest;
import com.github.dakusui.petronia.ut.PrognTest;
import com.github.dakusui.petronia.ut.QuoteTest;
import com.github.dakusui.petronia.ut.RuleSetTest;
import com.github.dakusui.petronia.ut.SimpleTestArrayGeneratorTest;
import com.github.dakusui.petronia.ut.StrTest;
import com.github.dakusui.petronia.ut.SymbolTest;
import com.github.dakusui.petronia.ut.UtilsTest;
import com.github.dakusui.petronia.ut.WhenTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		AlwaysTrueTest.class,
		AssignTest.class,
		CartesianTestArrayGeneratorTest.class,
		CompTest.class,
		ConcatTest.class,
		CondTest.class,
		ConsTest.class,
		ConsTest2.class,
		EqTest.class,
		EvalTest.class,
		FormatTest.class,
		FormEvalTest.class,
		IsInstanceOfTest.class,
		IsOneOfTest.class,
		IPOTest.class,
		Java.class,
		LambdaTest.class,
		LogicalPredicateTest.class,
		LoopTest.class,
		MaxMinTest.class,
		NumCastTest.class,
		NumericFuncTest.class,
		NumericTest.class,
		PrognTest.class,
		QuoteTest.class,
		RuleSetTest.class,
		SimpleTestArrayGeneratorTest.class,
		StrTest.class,
		SymbolTest.class,
		UtilsTest.class,
		WhenTest.class
})
public class UnitTests {}
