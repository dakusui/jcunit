package com.github.dakusui.lisj.forms.control;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.lisj.Basic;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class QuoteTest extends JCUnitBase {
  @Test
  public void quote_00() throws Exception {
    assertArrayEquals(new Object[] { }, (Object[]) (Basic.eval(this, quote())));
  }

  @Test
  public void quote_01() throws Exception {
    assertArrayEquals(new Object[] { 1 },
        (Object[]) Basic.eval(this, quote(1)));
  }

  @Test
  public void quote_02() throws Exception {
    assertArrayEquals(new Object[] { 1, 2 },
        (Object[]) (Basic.eval(this, quote(1, 2))));
  }

  @Test
  public void quote_03() throws Exception {
    assertArrayEquals(new Object[] { 1, 2, 3 },
        (Object[]) (Basic.eval(this, quote(1, 2, 3))));
  }

  @Test
  public void q_03() throws Exception {
    assertArrayEquals(new Object[] { 1, 2, 3 },
        (Object[]) (Basic.eval(this, q(1, 2, 3))));
  }
}
