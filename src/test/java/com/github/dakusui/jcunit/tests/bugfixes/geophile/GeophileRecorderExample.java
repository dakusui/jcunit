package com.github.dakusui.jcunit.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;
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
public class GeophileRecorderExample extends GeophileTestBase {
  private static final String   RECORDER_BASE = null;
  @Rule
  public               Recorder recorder      = new Recorder();

  public GeophileRecorderExample() {
    super(0, 0, 0);
  }

  @BeforeClass
  public static void beforeClass() {
    ////
    // Set the system property to 'true' for the sake of the test.
    System.setProperty(SystemProperties.KEY.RECORDER.key(), "true");

    File baseDir = Recorder
        .testClassDataDirFor(RECORDER_BASE, GeophileRecorderExample.class);
    if (baseDir.exists()) {
      IOUtils.deleteRecursive(baseDir);
    }
    assertTrue(!baseDir.exists());
  }

  @Test
  public void test() {
    File baseDir = Recorder
        .testClassDataDirFor(RECORDER_BASE, GeophileRecorderExample.class);
    assertNotNull(baseDir.list());
    assertThat(baseDir.list().length, is(this.recorder.getTestCase().getId() + 1));
  }
}
