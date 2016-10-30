package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.regex.RegexLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static java.lang.String.format;

@RunWith(JCUnit.class)
public class RegexExample {
  public static final int runCount     = 22;
  public static final int failureCount = 0;
  public static final int ignoreCount  = 0;

  @FactorField(levelsProvider = RegexLevelsProvider.class, args = {
      @Value({ "(Hello|hello)world everyone{0,1}(A|B|C)" })
  })
  public List<String> regex;

  @FactorField
  public int i;


  @Test
  public void test() {
    System.out.println(this.toString());
  }

  public String toString() {
    return format("i=%s; regex=%s", i, regex);
  }
}
