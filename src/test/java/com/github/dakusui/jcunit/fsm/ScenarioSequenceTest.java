package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScenarioSequenceTest {
  @Test
  @Ignore
  public void testEquals() {
    Story storyA = new Story(
        "storyA",
        new ScenarioSequence.BuilderFromTuple().setFSMName("storyA").build(),
        new ScenarioSequence.BuilderFromTuple().setFSMName("storyA").build()
    );

    Story storyB = new Story(
        "storyB",
        null,
        null
    );
    assertEquals(storyA, storyB);
  }
}
