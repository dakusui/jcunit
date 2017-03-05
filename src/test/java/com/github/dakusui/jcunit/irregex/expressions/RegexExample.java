package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.regex.RegexLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.TestDescription;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = IpoGcCoveringArrayEngine.class),
    checker = @Checker(value = SmartConstraintCheckerImpl.NoNegativeTests.class)
)
public class RegexExample {

  public static final int runCount     = 7;
  public static final int failureCount = 0;
  public static final int ignoreCount  = 0;

  @Rule
  public TestDescription testDescription = new TestDescription();

  private static final String INPUT = "(git)(" +
      "((clone)(URL)(DIRNAME){0,1})" +
      "|" + "pull" +
      "|" + "((push)(origin)(BRANCH{0,1}COLON_BRANCH{0,1}))" +
      ")";

  @SuppressWarnings("WeakerAccess")
  @FactorField(levelsProvider = RegexLevelsProvider.class, args = { @Value(INPUT) })
  public List<String> regex;

  @Test
  public void print() {
    System.out.println(testDescription.getTestCase().getCategory() + ":" + StringUtils.join(" ", this.regex));
  }
}
