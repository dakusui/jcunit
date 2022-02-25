package com.github.dakusui.jcunitx.tests.components.condition;

import com.github.dakusui.jcunitx.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.runners.core.NodeUtils;
import com.github.dakusui.jcunitx.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import static com.github.dakusui.jcunitx.testutils.UTUtils.print;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class NodeTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void givenTokens$whenAllLeaves$thenCorrectlyTokenized() {
    assertThat(
        NodeUtils.allLeaves(new String[] { "a", "b&&!c" }),
        equalTo(asList("a", "b", "c")));
  }

  @Test
  public void givenQuadraticEquationExampleClass$whenAllTestPredicates$thenKnownConstraintsAreReturned() {
    assertThat(
        print(NodeUtils.allTestPredicates(new TestClass(QuadraticEquationExample.class)).values()),
        allOf(
            hasItem(instanceOf(Constraint.class)),
            everyItem(instanceOf(Constraint.class))));
  }
}
