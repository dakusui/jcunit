package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import com.github.dakusui.jcunit.generators.RecordedTuplePlayer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
///*/
@TupleGeneration(
    generator = @Generator(
        value = RecordedTuplePlayer.class,
        params = @Param("FailedOnly")
    ))
///*/
public class ReplayerExample {
  @Rule
  public JCUnitRecorder recorder = new JCUnitRecorder();

  @FactorField
  int i;

  @FactorField
  int j;

  @FactorField
  int k;

  @Test
  public void test() {
    //    assertTrue(k > 0);
  }
}
