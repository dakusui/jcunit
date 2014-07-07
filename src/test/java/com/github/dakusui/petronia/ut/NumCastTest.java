package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.func.math.NumCast;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class NumCastTest extends JCUnitBase {
  @Test
  public void intValue() throws JCUnitException, CUT {
    assertEquals((int) 123, Basic.eval(this, intValue(123)));
  }

  @Test
  public void shortValue() throws JCUnitException, CUT {
    assertEquals((short) 123, Basic.eval(this, shortValue((short) 123)));
  }

  @Test
  public void longValue() throws JCUnitException, CUT {
    assertEquals((long) 123, Basic.eval(this, longValue(123)));
  }

  @Test
  public void floatValue() throws JCUnitException, CUT {
    assertEquals((float) 123.45, Basic.eval(this, floatValue(123.45)));
  }

  @Test
  public void doubleValue() throws JCUnitException, CUT {
    assertEquals((double) 123.45, Basic.eval(this, doubleValue(123.45)));
  }

  @Test
  public void byteValue() throws JCUnitException, CUT {
    assertEquals((byte) 123, Basic.eval(this, byteValue(123)));
  }

  @Test
  public void bigDecimal() throws JCUnitException, CUT {
    assertEquals(new BigDecimal(123), Basic.eval(this, bigDecimal(123)));
  }

  @Test
  public void bigInteger() throws JCUnitException, CUT {
    assertEquals(new BigInteger("123"), Basic.eval(this, bigInteger(123)));
  }

  @Test(
      expected = ClassCastException.class)
  public void illegalType() throws Exception {
    Basic.eval(this, intValue("XYZ"));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void nullValue() throws Exception {
    Basic.eval(this, Basic.eval(this, intValue(null)));
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void wrongNumArgth() throws Exception {
    Basic.eval(this, Basic.eval(this, new NumCast() {
      private static final long serialVersionUID = 1L;

      @Override
      protected Number cast(Number value) {
        return 678;
      }
    }.bind(123, 45)));
  }
}
