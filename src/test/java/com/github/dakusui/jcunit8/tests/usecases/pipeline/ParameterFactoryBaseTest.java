package com.github.dakusui.jcunit8.tests.usecases.pipeline;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * By extending Parameter.Factory.Base, this test is making sure that the class
 * is compilable when it is extended by a user.
 */
public class ParameterFactoryBaseTest extends Parameter.Factory.Base<String> {
  @Override
  public Parameter<String> create(String name) {
    return new Parameter.Simple.Impl<>(name, this.knownValues);
  }

  @Test
  public void compilableAndInstatiatable() {
    assertNotNull(this.create("hello"));
  }

  @Test
  public void whenAddActualValue$thenAdded() {
    Parameter<String> parameter = this.addActualValue("Hello").create("hello");
    FactorSpace factorSpace = parameter.toFactorSpace();

    assertEquals(1, factorSpace.getFactors().size());
    assertEquals("hello", factorSpace.getFactors().get(0).getName());
    assertEquals(singletonList("Hello"), factorSpace.getFactors().get(0).getLevels());
  }

  @Test
  public void whenAddActualValues$thenAdded() {
    Parameter<String> parameter = this.addActualValues(asList("Hello", "World", "VOID")).create("hello");
    FactorSpace factorSpace = parameter.toFactorSpace();

    assertEquals(1, factorSpace.getFactors().size());
    assertEquals("hello", factorSpace.getFactors().get(0).getName());
    assertEquals(asList("Hello", "World", "VOID"), factorSpace.getFactors().get(0).getLevels());
    assertEquals(asList("Hello", "World", "VOID"), parameter.getKnownValues());
  }
}
