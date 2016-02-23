package com.github.dakusui.jcunit.tests.examples.fsm.localconstraint;

import com.github.dakusui.jcunit.examples.localconstraint.LocalConstraintCheckerExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class LocalConstraintCheckerExampleTest extends Metatest {

  public LocalConstraintCheckerExampleTest() {
    super(LocalConstraintCheckerExample.class, 4, 0, 0);
  }

  @Test
  public void testLocalConstraintCheckerExample() {
    runTests();
  }
}
