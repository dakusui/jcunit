package com.github.dakusui.jcunit.framework.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.rules.Recorder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class GeophileRecorderTest extends GeophileTestBase {
  private static final String   RECORDER_BASE = null;
  @Rule
  public               Recorder recorder      = new Recorder();

  @BeforeClass
  public static void beforeClass() {
    ////
    // Set the system property to 'true' for the sake of the test.
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");

    File baseDir = Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    if (baseDir.exists()) {
      Utils.deleteRecursive(baseDir);
    }
    assertTrue(!baseDir.exists());
  }

  @Test
  public void test() {
    File baseDir = Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    assertNotNull(baseDir.list());
    assertThat(baseDir.list().length, is(this.recorder.getId() + 1));
  }
}
