package com.github.dakusui.jcunit.tests.features.fsm.localconstraint;

import com.github.dakusui.jcunit.examples.localconstraint.LocalConstraintCheckerExample;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LocalConstraintCheckerTest extends Metatest {
  @RunWith(JCUnit.class)
  public static class TestExecutor {
    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<Object, LocalConstraintCheckerExample.Spec> story;

    @Test
    public void test() {
      FSMUtils.performStory(
          this,
          "story",
          new SUTObject(),
          ScenarioSequence.Observer.Factory.ForSilent.INSTANCE
      );
    }
  }
  public static class SUTObject {
    private static int count = 0;
    /**
     * See {@code LocalConstraintCheckerExample.Spec}. The constraint checker is
     * implemented so that it excludes {@code null} from possible levels.
     */
    public boolean equals(Object another) {
      assertNotNull(another);
      count++;
      return super.equals(another);
    }
  }

  public LocalConstraintCheckerTest() {
    super(TestExecutor.class, 4, 0, 0);
  }

  @Test
  public void testLocalConstraintChecker() {
    SUTObject.count = 0;
    runTests();
    assertEquals(8, SUTObject.count);
  }

}
