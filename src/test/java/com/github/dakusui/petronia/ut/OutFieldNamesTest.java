package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutFieldNamesTest extends JCUnitBase {
  @Test
  public void test00a() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
    };
    assertEquals(0,
        ((Object[]) Basic.eval(this, this.outFieldNames(obj))).length);
  }

  @Test
  public void test00b() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      @In
      int dummy;
    };
    assertEquals(0,
        ((Object[]) Basic.eval(this, this.outFieldNames(obj))).length);
  }

  @Test
  public void test00c() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      int dummy;
    };
    assertEquals(0,
        ((Object[]) Basic.eval(this, this.outFieldNames(obj))).length);
  }

  @Test
  public void test01a() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      @Out
      public Object test1;
    };
    Object v = Basic.eval(this, this.outFieldNames(obj));
    Assert.assertEquals(1, Basic.length(v));
    Assert.assertEquals("test1", Basic.get(v, 0));
  }

  @Test
  public void test01b() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      @Out
      public Object test1;
      @SuppressWarnings("unused")
      public int dummy;
    };
    Object v = Basic.eval(this, this.outFieldNames(obj));
    Assert.assertEquals(1, Basic.length(v));
    Assert.assertEquals("test1", Basic.get(v, 0));
  }

  @Test
  public void test02a() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      @Out
      public Object test1;
      @SuppressWarnings("unused")
      @Out
      public Object test2;
    };
    assertTrue(Arrays.equals(new Object[] { "test1", "test2" },
        (Object[]) Basic.eval(this, this.outFieldNames(obj))));
  }

  @Test
  public void test02b() throws SymbolNotFoundException, JCUnitException, CUT {
    Object obj = new Object() {
      @SuppressWarnings("unused")
      @Out
      public Object test1;
      @SuppressWarnings("unused")
      @Out
      public int test2;
      @SuppressWarnings("unused")
      public int dummy;
    };
    assertTrue(Arrays.equals(new Object[] { "test1", "test2" },
        (Object[]) Basic.eval(this, this.outFieldNames(obj))));
  }
}
