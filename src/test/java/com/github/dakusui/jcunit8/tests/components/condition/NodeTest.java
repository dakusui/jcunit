package com.github.dakusui.jcunit8.tests.components.condition;

import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import static com.github.dakusui.jcunit8.testutils.UTUtils.print;
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
        equalTo(asList("a", "b", "c"))
    );
  }

  @Test
  public void givenQuadraticEquationExampleClass$whenAllTestPredicates$thenKnownConstraintsAreReturned() {
    assertThat(
        print(NodeUtils.allTestPredicates(new TestClass(QuadraticEquationExample.class)).values()),
        allOf(
            hasItem(
                instanceOf(Constraint.class)
            ),
            everyItem(
                instanceOf(Constraint.class)
            )
        )
    );
  }
}
