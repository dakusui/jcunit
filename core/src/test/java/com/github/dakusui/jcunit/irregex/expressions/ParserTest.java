package com.github.dakusui.jcunit.irregex.expressions;

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
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
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
  public static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter NAME_FORMATTER = new RegexTestUtils.ExprTreePrinter.InternalNodeFormatter() {
    @Override
    public String format(Expr.Composite expr) {
      return expr.name();
    }
  };
  public static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter ID_FORMATTER   = new RegexTestUtils.ExprTreePrinter.InternalNodeFormatter() {
    @Override
    public String format(Expr.Composite expr) {
      return expr.id();
    }
  };
  //String input = "git ((clone URL (dir){0,1})|(pull( origin BRANCH_PULLED_FROM){0,1})|(push origin BRANCH(_BRAHCH){0,1}))";
  @FactorField(stringLevels = {
      "(A){0,1}abc(B){0,1};abc,Aabc,abcB,AabcB;(*(*A){0,1}abc(*B){0,1})",    // 0: OK - 4; abc,A abc, abc B, A abc B
      "(A){0,1}abc(B){1,2};abcB,AabcB,abcBB,AabcBB;(*(*A){0,1}abc(*B){1,2})", // 1: OK - 4; abc, A abc B, abc BB, A abc BB
      "A(B|C);AB,AC;(*A(+BC))",                               // 2: OK - 2; AB, AC
      "(A)(B|C);AB,AC;(*(*A)(+BC))",                             // 3: OK - 2; AB, AC
      "(A)(B|C){0,1};A,AB,AC;(*(*A)(+BC){0,1})",                      // 4: OK - 3; A, AB, AC
      "A(B|C){0,1};A,AB,AC;(*A(+BC){0,1})",                        // 5: OK - 3; A, AB, AC
      "(A(B|C));AB,AC;(*(*A(+BC)))",                             // 6: NG - 2; AB, AC
      "(A(B|C)){0,1};,AB,AC;(*(*A(+BC)){0,1})",                       // 7: NG - 3; ,AB, AC
      "A(B(C){0,1}){0,1};A,AB,ABC;(*A(*B(*C){0,1}){0,1})",                          // 8:
      "(A B){0,1};,AB;(*(*A B){0,1})",                                 // 9:
      "A B{0,1};,AB;(*A B{0,1})",                                   //10:
      "A(B);AB;(*A(*B))",                                       //11:
      "A{0,2};,A,AA;(*A{0,2})",                               //12: OK - 3; ,A,AA
      "(A|B C);A,BC;(*(+AB C))",                                    //13:
      "git clone URL;gitcloneURL;(*git clone URL)",               //14: NG - 1; git clone URL
      "git;git;(*git)",
      "(A|B){2,3};AA,ABA,BBA,AAA,ABB,AB,AAB,BAA,BAB,BA;(*(+AB){2,3})",
      "git clone URL DIRNAME{0,1};_;_"
  })
  public String _input;

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
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("input");
    new Parser().parse(input()).accept(regexTestSuiteBuilder);
    RegexTestUtils.printTestSuite(regexTestSuiteBuilder.buildTestSuite());
  }

  @Test
  public void printGeneratedList() {
    FactorDef factorDef = new FactorDef.Regex("input", input());
    RegexTestSuiteBuilder regexTestSuiteBuilder = new RegexTestSuiteBuilder("input");
    new Parser().parse(input()).accept(regexTestSuiteBuilder);

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
  }

  @Test
  public void testInterpret() {
    //    System.out.println(this.input() + ":" + StringUtils.join(" ", Parser.interpret(this.input())));
    assertEquals(expectationForTokenizedStrings(), StringUtils.join("", Parser.interpret(this.input())));
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
