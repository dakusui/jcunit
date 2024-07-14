package com.github.dakusui.jcunit8.ut.regex;


import com.github.dakusui.jcunit8.ututiles.PipelineTestBase;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.core.regex.Expr;
import com.github.jcunit.core.regex.Parser;
import com.github.jcunit.testsuite.TestCase;
import com.github.jcunit.testsuite.TestSuite;
import com.github.jcunit.utils.InternalUtils;
import com.github.valid8j.pcond.forms.Predicates;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.valid8j.classic.TestAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest extends PipelineTestBase {
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter NAME_FORMATTER = Expr.Composite::name;
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter ID_FORMATTER   = Expr.Base::id;

  //String input = "git ((clone URL (dir){0,1})|(pull( origin BRANCH_PULLED_FROM){0,1})|(push origin BRANCH(_BRAHCH){0,1}))";
  //  @SuppressWarnings("WeakerAccess")
  public static Stream<String> data() {
    return Arrays.stream(new String[] {
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
      /*25*/ "((A{0,1})|B|(C{0,1}));,A,B,C;(*(+(*A{0,1})B(*C{0,1})))", // limitation; where multiple component can result in the same result can produce the same test cases.
      /*26*/ "((A{0,1})|B|(C{0,1}D{0,1}));,A,B,C,CD,D;(*(+(*A{0,1})B(*C{0,1}D{0,1})))", // limitation. see above
      /*27*/ "A|B|C;A,B,C;(+ABC)",
      /*28*/ "(A)|(B)|(C);A,B,C;(+(*A)(*B)(*C))",
      /*29*/ "git clone  URL(dir){0,1};gitcloneURL,gitcloneURLdir;(*git clone  URL(*dir){0,1})",
      /*30*/ "(A|B){0,2};,AA,BB,AB,A,B,BA;(*(+AB){0,2})",
      /*31*/ "(A|B){1,3};ABA,AA,BB,A,AAA,AB,AAB,B,BAA,BBB,BA;(*(+AB){1,3})",
    });
  }

  //private String _input;

  public ParserTest() {
    //this._input = input;
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parseTreePrintingWithId(String _input) {
    System.out.println("input expression:" + input(_input));
    new Parser().parse(input(_input)).accept(
        new RegexTestUtils.ExprTreePrinter(ID_FORMATTER)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parseTreePrintingWithName(String _input) {
    System.out.println("input expression:" + input(_input));
    new Parser().parse(input(_input)).accept(new RegexTestUtils.ExprTreePrinter(NAME_FORMATTER));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("data")
  public void printGeneratedList(String _input) {
    Parameter.Regex<String> parameter = Parameter.Regex.Factory.of(input(_input)).create("input");

    TestSuite builtTestSuite = generateTestSuite(parameter);
    Set<String> generatedStringsFromRegex = new HashSet<>();
    for (TestCase each : builtTestSuite) {
      generatedStringsFromRegex.add(
          InternalUtils.join(
              "",
              (List<String>) each.getTestData().get("input")
          ));
    }
    assertThat(generatedStringsFromRegex, generatedStringsMatcher(_input));
    builtTestSuite.forEach(System.out::println);
    assertEquals(expectationForGeneratedStrings(_input).split(",").length, builtTestSuite.size());
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testInterpret(String _input) {
    assertEquals(expectationForTokenizedStrings(_input),
                 InternalUtils.join("", Parser.preprocess(this.input(_input))));
  }

  /*
  private RegexTestSuiteBuilder getRegexTestSuiteBuilder() {ZZ
    //    new Parser().parse(input()).accept(regexTestSuiteBuilder);
    return new RegexTestSuiteBuilder("input", new Parser().parse(input()));
  }
  */


  private String input(String _input) {
    return _input.split(";")[0];
  }

  private Predicate<Set<String>> generatedStringsMatcher(String _input) {
/*
    return CoreMatchers.allOf(
        CoreMatchers.is(possibleStrings(expectationForGeneratedStrings(_input)))
    );

 */
    return Predicates.isEqualTo(possibleStrings(expectationForGeneratedStrings(_input)));
  }

  private Set<String> possibleStrings(String expectation) {
    return new HashSet<>(asList(expectation.split(",")));
  }

  private String expectationForGeneratedStrings(String _input) {
    return _input.split(";")[1];
  }

  private String expectationForTokenizedStrings(String _input) {
    return _input.split(";")[2];
  }
}
