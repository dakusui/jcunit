package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ScenarioSequenceTest {
  @Test
  public void testEquals() {
    Story storyA = new Story(
        "story",
        ScenarioSequence.EMPTY,
        ScenarioSequence.EMPTY
    );

    Story storyB = new Story(
        "story",
        ScenarioSequence.EMPTY,
        ScenarioSequence.EMPTY
    );
    assertEquals(storyA, storyB);
    assertEquals(storyB, storyA);
  }

  @Test
  public void testNotEquals() {
    Story storyA = new Story(
        "story",
        ScenarioSequence.EMPTY,
        ScenarioSequence.EMPTY
    );

    Story storyB = new Story(
        "STORY",
        ScenarioSequence.EMPTY,
        ScenarioSequence.EMPTY
    );
    assertNotEquals(storyA, storyB);
    assertNotEquals(storyB, storyA);
  }
}
