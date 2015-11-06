package com.github.dakusui.jcunit.tests.fsm;

import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.Story;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ScenarioSequenceTest {
  @Test
  public void testEquals() {
    Story storyA = new Story(
        "story",
        ScenarioSequence.Empty.getInstance(),
        ScenarioSequence.Empty.getInstance()
    );

    Story storyB = new Story(
        "story",
        ScenarioSequence.Empty.getInstance(),
        ScenarioSequence.Empty.getInstance()
    );
    assertEquals(storyA, storyB);
    assertEquals(storyB, storyA);
  }

  @Test
  public void testNotEquals() {
    Story storyA = new Story(
        "story",
        ScenarioSequence.Empty.getInstance(),
        ScenarioSequence.Empty.getInstance()
    );

    Story storyB = new Story(
        "STORY",
        ScenarioSequence.Empty.getInstance(),
        ScenarioSequence.Empty.getInstance()
    );
    assertNotEquals(storyA, storyB);
    assertNotEquals(storyB, storyA);
  }
}
