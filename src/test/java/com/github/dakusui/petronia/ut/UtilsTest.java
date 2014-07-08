package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.DomainGenerator;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {
  @In
  public String a = "field-a";

  @Out
  public String b = "field-b";

  public String c = "field-c";

  @In
  public int intField;

  @In
  public long longField;

  @In
  public short shortField;

  @In
  public byte byteField;

  @In
  public float floatField;

  @In
  public double doubleField;

  @In
  public char charField;

  @In
  public boolean booleanField;

  @In
  public String stringField;

  @In
  public Object objField;

  @In(
      domain = Domain.None)
  public Object noneField;

  @Test
  public void getFieldValue_01() {
    assertEquals("field-a",
        Utils.getFieldValue(this, Utils.getField(this, "a")));
  }

  @Test
  public void getFieldValue_02() {
    assertEquals("field-b",
        Utils.getFieldValue(this, Utils.getField(this, "b")));
  }

  @Test(
      expected = RuntimeException.class)
  public void getField_01() {
    Utils.getField(this, "c");
  }

  @Test(
      expected = RuntimeException.class)
  public void getField_03() {
    Utils.getField(this, "d");
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void bidDecimal_e01() {
    Utils.bigDecimal(new Number() {
      private static final long serialVersionUID = 1L;

      @Override
      public int intValue() {
        return 0;
      }

      @Override
      public long longValue() {
        return 0;
      }

      @Override
      public float floatValue() {
        return 0;
      }

      @Override
      public double doubleValue() {
        return 0;
      }
    });
  }

  @Test
  public void intDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "intField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Integer);
    }
  }

  @Test
  public void longDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "longField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Long);
    }
  }

  @Test
  public void shortDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "shortField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Short);
    }
  }

  @Test
  public void byteDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "byteField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Byte);
    }
  }

  @Test
  public void floatDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "floatField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Float);
    }
  }

  @Test
  public void doubleDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "doubleField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Double);
    }
  }

  @Test
  public void charDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "charField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Character);
    }
  }

  @Test
  public void booleanDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "booleanField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Boolean);
    }
  }

  @Test
  public void stringDomain() throws JCUnitException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "stringField"));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof String);
    }
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void objDomain() throws JCUnitException {
    JCUnit.domainGenerator(this.getClass(), Utils.getField(this, "objField"));
  }

  @Test
  public void noneDomain() throws JCUnitException {
    DomainGenerator dGenerator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "noneField"));
    assertEquals(0, dGenerator.domain().length);
  }
}
