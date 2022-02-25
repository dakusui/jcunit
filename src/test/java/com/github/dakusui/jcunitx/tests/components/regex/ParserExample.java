package com.github.dakusui.jcunitx.tests.components.regex;

import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.factorspace.FactorSpace;
import com.github.dakusui.jcunitx.metamodel.parameters.regex.RegexDecomposer;
import com.github.dakusui.jcunitx.regex.Expr;
import com.github.dakusui.jcunitx.regex.Parser;
import org.junit.Test;

import static java.util.stream.Collectors.joining;

@SuppressWarnings("NewClassNamingConvention")
public class ParserExample {
  private static final RegexTestUtils.ExprTreePrinter.InternalNodeFormatter ID_FORMATTER = Expr.Base::id;

  @Test
  public void test() {
    new Parser().parse("hello world").accept(new RegexTestUtils.ExprTreePrinter(ID_FORMATTER));
  }

  @Test
  public void test2() {
    new Parser().parse("hello|world").accept(new RegexTestUtils.ExprTreePrinter(ID_FORMATTER));
  }

  @Test
  public void test3() {
    new Parser().parse("hello,world").accept(new RegexTestUtils.ExprTreePrinter(ID_FORMATTER));
  }

  @Test
  public void test4() {
    new Parser().parse("hello(world|WORLD)everyone{0,1}").accept(new RegexTestUtils.ExprTreePrinter(ID_FORMATTER));
  }


  @Test
  public void test5() {
    FactorSpace factorSpace = new RegexDecomposer("top", new Parser().parse("hello(world|WORLD)everyone{0,1}")).decompose();
    System.out.println("= Factors");
    factorSpace.getFactors().forEach(each -> System.out.println(formatFactor(each)));
    System.out.println("= Constraints");
    factorSpace.getConstraints().forEach(System.out::println);
  }

  private String formatFactor(Factor factor) {
    return String.format("name: <%s>:[%s]", factor.getName(), factor.getLevels().stream().map(eachLevel -> String.format("%s(%s)", eachLevel, eachLevel.getClass())).collect(joining(",")));
  }
}
