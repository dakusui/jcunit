package com.github.dakusui.jcunit8.ut.regex;


import com.github.dakusui.jcunit8.ututils.PipelineTestBase;
import com.github.jcunit.core.regex.Expr;
import com.github.jcunit.core.regex.RegexParser;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.testsuite.TestCase;
import com.github.jcunit.testsuite.TestSuite;
import com.github.jcunit.utils.InternalUtils;
import com.github.valid8j.pcond.forms.Printables;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.valid8j.fluent.Expectations.*;
import static java.util.Arrays.asList;

public class RegexParserTest extends PipelineTestBase {
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter NAME_FORMATTER = Expr.Composite::name;
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter ID_FORMATTER = Expr.Base::id;

  /**
   * Returns a stream of test data string.
   * Each test data string contains the following items in this order joining by a semicolon (`;`).
   *
   * [source]
   * ----
   * 0: input
   * 1: expectationForGeneratedStrings
   * 2: expectationForTokenizedStrings
   * ----
   */
  public static Stream<String> data() {
    return Arrays.stream(new String[]{
        /*      input,                       expectationForGeneratedStrings      expectationForTokenizedStrings*/
        /* 0*/ "(A){0,1}abc(B){0,1};         abc,Aabc,abcB,AabcB;                (*(*A){0,1}abc(*B){0,1})",   // 0: OK - 4; abc,A abc, abc B, A abc B
        /* 1*/ "(A){0,1}abc(B){1,2};         abcB,AabcB,abcBB,AabcBB;            (*(*A){0,1}abc(*B){1,2})",   // 1: OK - 4; abc, A abc B, abc BB, A abc BB
        /* 2*/ "A(B|C);                      AB,AC;                              (*A(+BC))",                  // 2: OK - 2; AB, AC
        /* 3*/ "(A)(B|C);                    AB,AC;                              (*(*A)(+BC))",               // 3: OK - 2; AB, AC
        /* 4*/ "(A)(B|C){0,1};               A,AB,AC;                            (*(*A)(+BC){0,1})",          // 4: OK - 3; A, AB, AC
        /* 5*/ "A(B|C){0,1};                 A,AB,AC;                            (*A(+BC){0,1})",             // 5: OK - 3; A, AB, AC
        /* 6*/ "(A(B|C));                    AB,AC;                              (*(*A(+BC)))",               // 6: NG - 2; AB, AC
        /* 7*/ "(A(B|C)){0,1};               ,AB,AC;                             (*(*A(+BC)){0,1})",          // 7: NG - 3; ,AB, AC
        /* 8*/ "A(B(C){0,1}){0,1};           A,AB,ABC;                           (*A(*B(*C){0,1}){0,1})",     // 8:
        /* 9*/ "(AB){0,1};                   ,AB;                                (*(*AB){0,1})",              // 9:
        /*10*/ "(A)(B){0,1};                 A,AB;                               (*(*A)(*B){0,1})",           //10:
        /*11*/ "A(B);                        AB;                                 (*A(*B))",                   //11:
        /*12*/ "A{0,2};                      ,A,AA;                              (*A{0,2})",                  //12: OK - 3; ,A,AA
        /*13*/ "(git)(clone)(URL);           gitcloneURL;                        (*(*git)(*clone)(*URL))",    //14: NG - 1; git clone URL
        /*14*/ "git;                         git;                                (*git)",
        /*15*/ "(A|B){2,3};                  BBA,AA,BBB,BB,AAA,AAB;              (*(+AB){2,3})",
        /*16*/ "A{1,2};                      A,AA;                               (*A{1,2})",
        /*17*/ "(A|B){0,1};                  ,A,B;                               (*(+AB){0,1})",
        /*18*/ "A{0,1}B{0,1};                ,A,B,AB;                            (*A{0,1}B{0,1})",
        /*19*/ "A;                           A;                                  (*A)",
        /*20*/ "(A);                         A;                                  (*(*A))",
        /*21*/ "(A)(A);                      AA;                                 (*(*A)(*A))",
        /*22*/ "(A|B);                       A,B;                                (*(+AB))",
        /*23*/ "(A|B|C);                     A,B,C;                              (*(+ABC))",
        /*24*/ "(A|B|(C(D{0,1})));           A,B,CD,C;                           (*(+AB(*C(*D{0,1}))))",
        /*25*/ "((A{0,1})|B|(C{0,1}));       ,A,B,C;                             (*(+(*A{0,1})B(*C{0,1})))", // limitation; where multiple component can result in the same result can produce the same test cases.
        /*26*/ "((A{0,1})|B|(C{0,1}D{0,1})); ,A,B,C,CD,D;                        (*(+(*A{0,1})B(*C{0,1}D{0,1})))", // limitation. see above
        /*27*/ "A|B|C;                       A,B,C;                              (+ABC)",
        /*28*/ "(A)|(B)|(C);                 A,B,C;                              (+(*A)(*B)(*C))",
        /*29*/ "git clone  URL(dir){0,1};    gitcloneURL,gitcloneURLdir;         (*git clone  URL(*dir){0,1})",
        /*30*/ "(A|B){0,2};                  ,AA,BB,AB,A,B,BA;                   (*(+AB){0,2})",
        /*31*/ "(A|B){1,3};                  ABA,AA,BB,A,AAA,AB,AAB,B,BAA,BBB,BA;(*(+AB){1,3})",
    });
  }

  public RegexParserTest() {
  }

  /**
   * A test for debugging.
   *
   * This prints an internal expression (a parsed tree) of an input.
   * An id of each node will be found in the output.
   * The expectation is extracted from the `_input`, which is given by the `RegexParserTest#data()`.
   *
   * @param _input the input data
   * @see RegexParserTest#data()
   */
  @ParameterizedTest
  @MethodSource("data")
  public void parseTreePrintingWithId(String _input) {
    System.out.println("input expression:" + input(_input));
    new RegexParser().parse(input(_input)).accept(
        new RegexTestUtils.ExprTreePrinter(ID_FORMATTER)
    );
  }

  /**
   * A test for debugging.
   *
   * This prints an internal expression (a parsed tree) of an input.
   * A name of each node will be found in the output.
   * The expectation is extracted from the `_input`, which is given by the `RegexParserTest#data()`.
   *
   * @param _input the input data
   * @see RegexParserTest#data()
   */
  @ParameterizedTest
  @MethodSource("data")
  public void parseTreePrintingWithName(String _input) {
    System.out.println("input expression:" + input(_input));
    new RegexParser().parse(input(_input)).accept(new RegexTestUtils.ExprTreePrinter(NAME_FORMATTER));
  }

  /**
   * This test checks if the generated sequences contain all the desired unique tokens through generating the test suite
   * from a parameter space containing a regex parameter.
   * The expectation is extracted from the `_input`, which is given by the `RegexParserTest#data()`.
   *
   * @param _input An input string
   * @see RegexParserTest#data()
   */
  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("data")
  public void whenGenerateSequence_thenTokensInSequenceAreEqualToExpectation(String _input) {
    Parameter.Regex<String> parameter = Parameter.Regex.Factory.of(input(_input)).create("input");
    TestSuite builtTestSuite = generateTestSuite(parameter);

    Set<String> tokensInSequenceGeneratedFromRegex = new HashSet<>();
    for (TestCase each : builtTestSuite) {
      tokensInSequenceGeneratedFromRegex.add(InternalUtils.joinBy("", (List<String>) each.getTestData().get("input")));
    }

    assertAll(value(tokensInSequenceGeneratedFromRegex).toBe()
                                                       .equalTo(sequencesToBeGenerated(_input)));
  }

  /**
   * Examine the internal expression of the input regular expression.
   * The expectation is extracted from the `_input`, which is given by the `RegexParserTest#data()`.
   *
   * @param _input An input string
   * @see RegexParserTest#data()
   */
  @ParameterizedTest
  @MethodSource("data")
  public void whenPreprocessed_thenResultMatchesExpectation(String _input) {
    List<String> preprocessedTokens = RegexParser.preprocess(this.input(_input));

    assertStatement(value(preprocessedTokens).function(joinBy(""))
                                             .toBe()
                                             .equalTo(expectedInternalExpressionByPreprocessing(_input)));
  }

  private static Function<List<String>, String> joinBy(String delimiter) {
    return Printables.function("joinBy[]", v -> String.join(delimiter, v));
  }

  private static String input(String _input) {
    return _input.split(";")[0].trim();
  }

  private static Set<String> sequencesToBeGenerated(String _input) {
    return new HashSet<>(asList(stringSequencesToBeGenerated(_input).split(",")));
  }

  private static String stringSequencesToBeGenerated(String _input) {
    return _input.split(";")[1].trim();
  }

  private static String expectedInternalExpressionByPreprocessing(String _input) {
    return _input.split(";")[2].trim();
  }
}
