package com.github.dakusui.jcunit.tests.fsm;

import com.github.dakusui.jcunit.fsm.SUTFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SUTFactoryTest {
  @Test
  public void testarraytypes() {
    assertEquals("class [J", SUTFactory.Base.LONG_ARRAY_TYPE.toString());
    assertEquals("class [Z", SUTFactory.Base.BOOLEAN_ARRAY_TYPE.toString());
    assertEquals("class [I", SUTFactory.Base.INT_ARRAY_TYPE.toString());
    assertEquals("class [B", SUTFactory.Base.BYTE_ARRAY_TYPE.toString());
    assertEquals("class [C", SUTFactory.Base.CHAR_ARRAY_TYPE.toString());
    assertEquals("class [F", SUTFactory.Base.FLOAT_ARRAY_TYPE.toString());
    assertEquals("class [D", SUTFactory.Base.DOUBLE_ARRRAY_TYPE.toString());
  }
}
