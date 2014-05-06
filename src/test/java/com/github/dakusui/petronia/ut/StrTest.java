package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;

public class StrTest extends JCUnitBase {
  @Test
  public void matches_01() throws Exception {
    assertEquals(true, Basic.eval(this, matches("abc", "abc")));
  }

  @Test
  public void matches_02() throws Exception {
    assertEquals(false, Basic.eval(this, matches("abc", "ab")));
  }

  @Test
  public void matches_03() throws Exception {
    assertEquals(false, Basic.eval(this, matches("abc", "abcd")));
  }

  @Test
  public void matches_04() throws Exception {
    assertEquals(true, Basic.eval(this, matches("abc", ".b.")));
  }

  @Test
  public void contains_01() throws Exception {
    assertEquals(true, Basic.eval(this, contains("abc", "a")));
  }

  @Test
  public void contains_02() throws Exception {
    assertEquals(false, Basic.eval(this, contains("abc", "B")));
  }
}
