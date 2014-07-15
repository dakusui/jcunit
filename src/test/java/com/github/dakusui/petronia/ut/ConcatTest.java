package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcatTest extends JCUnitBase {
  @Test
  public void concat_00() throws Exception {
    assertEquals("", Basic.eval(this, concat("")));
  }

  @Test
  public void concat_01() throws Exception {
    assertEquals("a", Basic.eval(this, concat("", "a")));
  }

  @Test
  public void concat_02() throws Exception {
    assertEquals("ab", Basic.eval(this, concat("", "a", "b")));
  }

  @Test
  public void concat_03() throws Exception {
    assertEquals("abc", Basic.eval(this, concat("", "a", "b", "c")));
  }

  @Test
  public void concat_10() throws Exception {
    assertEquals("", Basic.eval(this, concat(null)));
  }

  @Test
  public void concat_11() throws Exception {
    assertEquals("a", Basic.eval(this, concat(null, "a")));
  }

  @Test
  public void concat_12() throws Exception {
    assertEquals("ab", Basic.eval(this, concat(null, "a", "b")));
  }

  @Test
  public void concat_13() throws Exception {
    assertEquals("abc", Basic.eval(this, concat(null, "a", "b", "c")));
  }

  @Test
  public void concat_20() throws Exception {
    assertEquals("", Basic.eval(this, concat(";")));
  }

  @Test
  public void concat_21() throws Exception {
    assertEquals("a", Basic.eval(this, concat(";", "a")));
  }

  @Test
  public void concat_22() throws Exception {
    assertEquals("a;b", Basic.eval(this, concat(";", "a", "b")));
  }

  @Test
  public void concat_23() throws Exception {
    assertEquals("a;b;c", Basic.eval(this, concat(";", "a", "b", "c")));
  }

  @Test
  public void concat_30() throws Exception {
    assertEquals("", Basic.eval(this, concat("---")));
  }

  @Test
  public void concat_31() throws Exception {
    assertEquals("a", Basic.eval(this, concat("---", "a")));
  }

  @Test
  public void concat_32() throws Exception {
    assertEquals("a---b", Basic.eval(this, concat("---", "a", "b")));
  }

  @Test
  public void concat_33() throws Exception {
    assertEquals("a---b---c", Basic.eval(this, concat("---", "a", "b", "c")));
  }

}
