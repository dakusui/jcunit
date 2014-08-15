package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import com.github.dakusui.jcunit.generators.RecordedTuplePlayer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = RecordedTuplePlayer.class,
        params = @Param("FailedOnly")
    ))
public class ReplayerExample {
  @Rule
  public JCUnitRecorder recorder = new JCUnitRecorder();

  @FactorField
  public int i;

  @FactorField
  public int j;

  @FactorField
  public int k;

  @BeforeClass
  public static void beforeClass() {
    JCUnitRecorder.initializeDir(ReplayerExample.class);
  }

  @Test
  public void testX() {
    assertTrue(k > 0);
  }

  @Test
  public void testY() {
    assertTrue(k > 100);
  }
}
