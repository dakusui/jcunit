package com.github.dakusui.jcunit.tests.features.fsm.outputchecking;

import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.SUTFactory;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;

@RunWith(JCUnit.class)
public class CounterInteractionHistoryWithConstructorTest {
  @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = @Value({ "1" }))
  public Story<Counter, CounterInteractionHistoryTest.CounterSpec> story;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test() {
    FSMUtils.performStory(this, "story", new SUTFactory.Simple<Counter>(
        Counter.class,
        $(int.class, 100).as("offset")
    ).as("test"));
  }

}
