package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.regex.RegexLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class RegexExample {
  public static final int runCount     = 22;
  public static final int failureCount = 0;
  public static final int ignoreCount  = 0;
  @Rule
  public TestDescription testDescription = new TestDescription();

  //  private static final String INPUT = "(Hello|hello)world everyone{0,1}(A|B|C)";
  //private static final String INPUT = "(Hello|hello)world (everyone{0,1}(A|B|C){1,2}){0,1}";
  private static final String INPUT = "git clone URL DIRNAME{0,1}";
  //      "git(clone(URL(DIRNAME){0,1}))";

  @FactorField(levelsProvider = RegexLevelsProvider.class, args = { @Value(INPUT) })
  public List<String> regex;

  //  @FactorField
  //  public int i;


  @Test
  public void print() {
    System.out.println(testDescription.getFactors().asFactorList());
    System.out.println(StringUtils.join(" ", this.regex));
  }

  //  @Test
  public void test() {
    assertTrue(StringUtils.join(" ", regex).matches(INPUT));
  }

  //  public String toString() {
  //    return format("i=%s; regex=%s", i, regex);
  //  }
}
