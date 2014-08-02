package com.github.dakusui.jcunit.framework.tests.bugfixes.geophile;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class GeophileRecorderTest extends GeophileTestBase {
  private static final String RECORDER_BASE = "src/test/resources";
  @Rule
  public JCUnitRecorder recorder = new JCUnitRecorder(RECORDER_BASE);

  @BeforeClass
  public static void beforeClass() {
    File baseDir =Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    Utils.deleteRecursive(baseDir);
    assertTrue(!baseDir.exists());
  }

  @Test
  public void test() {
  }

  @AfterClass
  public static void afterClass() {
    File baseDir =Utils.baseDirFor(RECORDER_BASE, GeophileRecorderTest.class);
    assertTrue(baseDir.exists());
  }
}
