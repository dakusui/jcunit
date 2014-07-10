package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.DomainGenerator;
import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.LisjUtils;
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
        Utils.getFieldValue(this, Utils.getField(this, "a", In.class)));
  }

  @Test
  public void getFieldValue_02() {
    assertEquals("field-b",
        Utils.getFieldValue(this, Utils.getField(this, "b", Out.class)));
  }

  @Test(
      expected = RuntimeException.class)
  public void getField_01() {
    Utils.getField(this, "c", In.class);
  }

  @Test(
      expected = RuntimeException.class)
  public void getField_03() {
    Utils.getField(this, "d", Out.class);
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void bidDecimal_e01() {
    LisjUtils.bigDecimal(new Number() {
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
  public void intDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "intField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Integer);
    }
  }

  @Test
  public void longDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "longField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Long);
    }
  }

  @Test
  public void shortDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "shortField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Short);
    }
  }

  @Test
  public void byteDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "byteField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Byte);
    }
  }

  @Test
  public void floatDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "floatField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Float);
    }
  }

  @Test
  public void doubleDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "doubleField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Double);
    }
  }

  @Test
  public void charDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "charField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Character);
    }
  }

  @Test
  public void booleanDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "booleanField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof Boolean);
    }
  }

  @Test
  public void stringDomain() throws JCUnitCheckedException {
    DomainGenerator dGeneator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "stringField", In.class));
    assertNotNull(dGeneator);
    for (Object cur : dGeneator.domain()) {
      assertTrue(cur == null || cur instanceof String);
    }
  }

  @Test(
      expected = IllegalArgumentException.class)
  public void objDomain() throws JCUnitCheckedException {
    JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "objField", In.class));
  }

  @Test
  public void noneDomain() throws JCUnitCheckedException {
    DomainGenerator dGenerator = JCUnit.domainGenerator(this.getClass(),
        Utils.getField(this, "noneField", In.class));
    assertEquals(0, dGenerator.domain().length);
  }
}
