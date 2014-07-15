package com.github.dakusui.jcunit.extras.experiments;

import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.annotations.Generator;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.constraints.Constraint;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.generators.CartesianTestArrayGenerator;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(CartesianTestArrayGenerator.class)
public class PictExample1 extends JCUnitBase {
  @In(domain = Domain.Method)
  public String edition;

  public static String[] edition() {
    return new String[] { "Ultimate", "Business", "Home Premium",
        "Home Basic" };
  }

  @In(domain = Domain.Method)
  public String ram;

  public static String[] ram() {
    return new String[] { "0.5G", "1G", "1.5G", "2G" };
  }

  @In(domain = Domain.Method)
  public String gram;

  public static String[] gram() {
    return new String[] { "64M", "128M", "256M", "512M" };
  }

  @In(domain = Domain.Method)
  public String hd1;

  public static String[] hd1() {
    return hd();
  }

  @In(domain = Domain.Method)
  public String hd2;

  public static String[] hd2() {
    return hd();
  }

  private static String[] hd() {
    return new String[] { "20G", "30G", "50G", "100G", "None" };
  }

  @In(domain = Domain.Method)
  public String cpu;

  public static String[] cpu() {
    return new String[] { "0.8G", "1G", "1.5G", "2G" };
  }

  @In(domain = Domain.Method)
  public String browser;

  public static String[] browser() {
    return new String[] { "IE", "Firefox", "Opera", "Safari" };
  }

  @Constraint
  public RuleSet constraint = this.ruleSet()
      .incase(eq($("hd1"), "None"), not(eq($("hd2"), "None")))
      .incase(eq($("hd2"), "None"), not(eq($("hd1"), "None")));
}
