package com.github.dakusui.jcunit.tests.features.fsm.outputchecking;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.As;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class CounterInteractionHistoryTest {
  public enum CounterSpec implements FSMSpec<Counter> {
    @StateSpec I;

    @ActionSpec
    @As("count")
    public Expectation<Counter> increment(Expectation.Builder<Counter> builder) {
      return builder.valid(I).build();
    }

    @ActionSpec
    public Expectation<Counter> get(Expectation.Builder<Counter> builder) {
      return builder.valid(I, new OutputChecker.ForInteractionHistory(Output.Type.VALUE_RETURNED) {
        @Override
        protected Object computeExpectation(InteractionHistory interactionHistory) {
          return interactionHistory.get("count").size() +
              (interactionHistory.has("offset")
                  ? (Integer) interactionHistory.get("offset").get(0)
                  : 0);
        }
      }).build();
    }

    @Override
    public boolean check(Counter counter) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class, args = @Value({ "1" }))
  public Story<Counter, CounterSpec> story;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test() {
    FSMUtils.performStory(this, "story", new Counter());
  }
}
