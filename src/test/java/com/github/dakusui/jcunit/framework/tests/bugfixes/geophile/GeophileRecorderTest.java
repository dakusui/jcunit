package com.github.dakusui.jcunit.framework.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
public class GeophileRecorderTest extends GeophileTestBase {
  private static final String RECORDER_BASE = null;
  @Rule
  public JCUnitRecorder recorder = new JCUnitRecorder();

  @BeforeClass
  public static void beforeClass() {
    File baseDir =Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    if (baseDir.exists()) {
      Utils.deleteRecursive(baseDir);
    }
    assertTrue(!baseDir.exists());
  }

  @Test
  public void test() {
    File baseDir =Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    assertThat(baseDir.list().length, is(this.recorder.getId() + 1));
  }
}
