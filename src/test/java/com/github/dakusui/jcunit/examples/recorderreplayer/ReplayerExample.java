package com.github.dakusui.jcunit.examples.recorderreplayer;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.annotations.Generator;
import com.github.dakusui.jcunit.annotations.Param;
import com.github.dakusui.jcunit.annotations.TupleGeneration;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.Recorder;
import com.github.dakusui.jcunit.generators.Replayer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = Replayer.class,
        params = @Param("All")
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
