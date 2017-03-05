package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit.regex.RegexTestSuiteBuilder;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(JCUnit.class)
public class ParserTest {
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter NAME_FORMATTER = new RegexTestUtils.ExprTreePrinter.InternalNodeFormatter() {
    @Override
    public String format(Expr.Composite expr) {
      return expr.name();
    }
  };
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter ID_FORMATTER   = new RegexTestUtils.ExprTreePrinter.InternalNodeFormatter() {
    @Override
    public String format(Expr.Composite expr) {
      return expr.id();
    }
  };
  //String input = "git ((clone URL (dir){0,1})|(pull( origin BRANCH_PULLED_FROM){0,1})|(push origin BRANCH(_BRAHCH){0,1}))";
  @SuppressWarnings("WeakerAccess")
  @FactorField(stringLevels = {
      /* 0*/ "(A){0,1}abc(B){0,1};abc,Aabc,abcB,AabcB;(*(*A){0,1}abc(*B){0,1})",    // 0: OK - 4; abc,A abc, abc B, A abc B
      /* 1*/ "(A){0,1}abc(B){1,2};abcB,AabcB,abcBB,AabcBB;(*(*A){0,1}abc(*B){1,2})", // 1: OK - 4; abc, A abc B, abc BB, A abc BB
      /* 2*/ "A(B|C);AB,AC;(*A(+BC))",                               // 2: OK - 2; AB, AC
      /* 3*/ "(A)(B|C);AB,AC;(*(*A)(+BC))",                             // 3: OK - 2; AB, AC
      /* 4*/ "(A)(B|C){0,1};A,AB,AC;(*(*A)(+BC){0,1})",                      // 4: OK - 3; A, AB, AC
      /* 5*/ "A(B|C){0,1};A,AB,AC;(*A(+BC){0,1})",                        // 5: OK - 3; A, AB, AC
      /* 6*/ "(A(B|C));AB,AC;(*(*A(+BC)))",                             // 6: NG - 2; AB, AC
      /* 7*/ "(A(B|C)){0,1};,AB,AC;(*(*A(+BC)){0,1})",                       // 7: NG - 3; ,AB, AC
      /* 8*/ "A(B(C){0,1}){0,1};A,AB,ABC;(*A(*B(*C){0,1}){0,1})",                          // 8:
      /* 9*/ "(AB){0,1};,AB;(*(*AB){0,1})",                                 // 9:
      /*10*/ "(A)(B){0,1};A,AB;(*(*A)(*B){0,1})",                                   //10:
      /*11*/ "A(B);AB;(*A(*B))",                                       //11:
      /*12*/ "A{0,2};,A,AA;(*A{0,2})",                               //12: OK - 3; ,A,AA
      /*13*/ "(git)(clone)(URL);gitcloneURL;(*(*git)(*clone)(*URL))",               //14: NG - 1; git clone URL
      /*14*/ "git;git;(*git)",
      /*15*/ "(A|B){2,3};BBA,AA,BBB,BB,AAA,AAB;(*(+AB){2,3})",
      /*16*/ "A{1,2};A,AA;(*A{1,2})",
      /*17*/ "(A|B){0,1};,A,B;(*(+AB){0,1})",
      /*18*/ "A{0,1}B{0,1};,A,B,AB;(*A{0,1}B{0,1})",
      /*19*/ "A;A;(*A)",
      /*20*/ "(A);A;(*(*A))",
      /*21*/ "(A)(A);AA;(*(*A)(*A))",
      /*22*/ "(A|B);A,B;(*(+AB))",
      /*23*/ "(A|B|C);A,B,C;(*(+ABC))",
      /*24*/ "(A|B|(C(D{0,1})));A,B,CD,C;(*(+AB(*C(*D{0,1}))))",
      /*25*/ "((A{0,1})|B|(C{0,1}));,,A,B,C;(*(+(*A{0,1})B(*C{0,1})))", // limitation; where multiple component can result in the same result can produce the same test cases.
      /*26*/ "((A{0,1})|B|(C{0,1}D{0,1}));,,A,B,C,CD,D;(*(+(*A{0,1})B(*C{0,1}D{0,1})))" // limitation. see above
  })
  public String _input;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void parseTreePrintingWithId() {
    System.out.println("input expression:" + input());
    new Parser().parse(input()).accept(new RegexTestUtils.ExprTreePrinter(ID_FORMATTER));
  }

  @Test
  public void parseTreePrintingWithName() {
    System.out.println("input expression:" + input());
    new Parser().parse(input()).accept(new RegexTestUtils.ExprTreePrinter(NAME_FORMATTER));
  }

  @Test
  public void printTestSuite() {
    RegexTestSuiteBuilder regexTestSuiteBuilder = getRegexTestSuiteBuilder();
    RegexTestUtils.printTestSuite(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void printGeneratedList() {
    FactorDef factorDef = new FactorDef.Regex("input", input());
    RegexTestSuiteBuilder regexTestSuiteBuilder = getRegexTestSuiteBuilder();

    FactorSpace factorSpace = new FactorSpace.Builder()
        .addFactorDefs(singletonList(factorDef))
        .build();

    TestSuite builtTestSuite = regexTestSuiteBuilder.buildTestSuite();
    for (TestCase each : builtTestSuite) {
      System.out.println(factorSpace.convert(each.getTuple()));
    }

    Set<String> generatedStringsFromRegex = new HashSet<String>();
    for (TestCase each : builtTestSuite) {
      generatedStringsFromRegex.add(
          StringUtils.join(
              "",
              (List) factorSpace.convert(each.getTuple())
                  .get("input")
          ));
    }
    assertThat(generatedStringsFromRegex, generatedStringsMatcher());
    assertEquals(expectationForGeneratedStrings().split(",").length, builtTestSuite.size());
  }

  @Test
  public void testInterpret() {
    //    System.out.println(this.input() + ":" + StringUtils.join(" ", Parser.preprocess(this.input())));
    assertEquals(expectationForTokenizedStrings(), StringUtils.join("", Parser.preprocess(this.input())));
  }

  @Test
  public void printFactorSpace() {
    FactorDef factorDef = new FactorDef.Regex("input", input());
    FactorSpace factorSpace = new FactorSpace.Builder()
        .addFactorDefs(singletonList(factorDef))
        .build();

    for (Factor each : factorSpace.factors) {
      System.out.println(each.name + ":" + each.levels);
    }
/*
    for (Constraint each : factorSpace.constraintChecker.getConstraints()) {
      System.out.println(each);
      System.out.println("  " + each.getFactorNamesInUse());
    }
    */
  }

  private RegexTestSuiteBuilder getRegexTestSuiteBuilder() {
    //    new Parser().parse(input()).accept(regexTestSuiteBuilder);
    return new RegexTestSuiteBuilder("input", new Parser().parse(input()));
  }


  private String input() {
    return this._input.split("\\;")[0];
  }

  private Matcher generatedStringsMatcher() {
    return CoreMatchers.allOf(
        CoreMatchers.is(possibleStrings(expectationForGeneratedStrings()))
    );
  }

  private Set<String> possibleStrings(String expectation) {
    Set<String> ret = new HashSet<String>();
    ret.addAll(asList(expectation.split(",")));
    return ret;
  }

  private String expectationForGeneratedStrings() {
    return this._input.split(";")[1];
  }

  private String expectationForTokenizedStrings() {
    return this._input.split(";")[2];
  }
}
