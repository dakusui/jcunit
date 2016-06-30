package com.github.dakusui.jcunit.examples.recorderreplayer;

import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;
import com.github.dakusui.jcunit.runners.standard.plugins.Replayer;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: This example needs to be re-designed soon.
 */
@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(
        value = Replayer.class,
        args = {
            @Value("com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine"),
            @Value("Replay"),
            @Value("All")
        }
    ))
public class ReplayerExample {
  @Rule
  public Recorder recorder = new Recorder();

  @FactorField
  public int i;

  @FactorField
  public int j;

  @FactorField
  public int k;

  @Recorder.Record
  public int x = 100;

  @Recorder.Record
  public int y = 101;

  @BeforeClass
  public static void beforeClass() {
    Recorder.initializeTestClassDataDir(ReplayerExample.class);
  }

  @Test
  public void testX() {
    recorder.save(this);
    ReplayerExample previous;
    if ((previous = recorder.load())!= null) {
      assertEquals(this.x, previous.x);
      assertEquals(this.y, previous.y);
    }
  }

  @Test
  public void testY() {
    //    assertTrue(k > 100);
  }
}
