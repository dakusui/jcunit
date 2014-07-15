package com.github.dakusui.jcunit.extras.ut;

import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class RuleSetTest extends JCUnitBase {
  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_01() throws Exception {
    ruleSet().expect("");
  }

  @Test
  public void ruleSet_02() throws Exception {
    assertNotNull(ruleSet().incase("").expect("") instanceof RuleSet);
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_03() throws Exception {
    // //
    // once 'expected' is set, you can't do it again until
    // 'incase' is called.
    ruleSet().incase("").expect("").expect("");
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_04() throws Exception {
    // //
    // Until 'expected' is called, you can't call 'cut'.
    ruleSet().cut();
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_05() throws Exception {
    // //
    // Until 'expected' is called, you can't call 'cut'.
    ruleSet().incase("").cut();
  }

  @Test
  public void ruleSet_06() throws Exception {
    // //
    // Only after expect is called, you can call cut.
    assertNotNull(ruleSet().incase("").expect("").cut());
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_07() throws Exception {
    // //
    // Only after expect is called, you can call cut.
    // But you can't call it twice.
    ruleSet().incase("").expect("").cut().cut();
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_08() throws Exception {
    // //
    // Without 'expect', you can't call 'incase' again.
    ruleSet().incase("").incase("");
  }

  @Test
  public void ruleSet_09() throws Exception {
    // //
    // You become able to call 'incase' again after calling 'expect'.
    ruleSet().incase("").expect("").incase("");
  }

  @Test
  public void ruleSet_10() throws Exception {
    // //
    // You become able to call 'incase' again after calling 'expect' and 'cut'.
    ruleSet().incase("").expect("").cut().incase("");
  }

  @Test
  public void ruleSet_51() throws Exception {
    // //
    // Once you call incase and expect, you become able to call 'otherwise'.
    assertNotNull(ruleSet().incase("").expect("").otherwise(""));
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_52() throws Exception {
    // //
    // Once you call 'otherwise', you will not be able to call 'incase' anymore.
    ruleSet().incase("", "").otherwise("").otherwise("");
  }

  @Test(
      expected = IllegalStateException.class)
  public void ruleSet_53() throws Exception {
    // //
    // Until 'incase' is called at least once, 'otherwise' can't be called.
    ruleSet().otherwise("");
  }

}
