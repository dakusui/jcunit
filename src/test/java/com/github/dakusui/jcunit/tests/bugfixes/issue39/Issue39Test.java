package com.github.dakusui.jcunit.tests.bugfixes.issue39;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;
import org.junit.runner.RunWith;


public class Issue39Test extends Metatest {
  @RunWith(JCUnit.class)
  public static class TestExecutor {
    public enum Spec implements FSMSpec<String> {
      @StateSpec I {
        // We should not need to place "@ActionSpec" here.
        @Override
        public Expectation<String> toString(Expectation.Builder<String> b) {
          return b.valid(J).build();
        }
      },
      @StateSpec J {
        // We should not need to place "@ActionSpec" here.
        @Override
        public Expectation<String> toString(Expectation.Builder<String> b) {
          return b.valid(I).build();
        }
      };

      @ActionSpec
      public Expectation<String> toString(Expectation.Builder<String> b) {
        return  b.invalid().build();
      }

      @Override
      public boolean check(String s) {
        return true;
      }
    }

    @FactorField(levelsProvider = FSMLevelsProvider.class)
    public Story<String, Spec> story;

    @Test
    public void test() {
      FSMUtils.performStory(this, "story", "Hello", ScenarioSequence.Observer.Factory.ForSilent.INSTANCE);
    }
  }

  public Issue39Test() {
    ////
    // Test case 1: I -> J
    // Test case 2: J -> I
    //
    // If the above defined test spec doesn't work, test suite will not be generated at all
    // because the "toString(Expectation.Builder)" method is abstract.
    super(TestExecutor.class, 2, 0, 0);
  }


  @Test
  public void testIssue39() {
    runTests();
  }
}
