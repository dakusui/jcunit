package com.github.dakusui.jcunit.extras.forms;

import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.lisj.Form;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;

public abstract class AutoFunctionTest extends FormTestBase {
  public static class Example {
    @Out
    public int example = 100;

    public Example(int i) {
      this.example = i;
    }
  }

  @Rule
  public TestName testName = new TestName();

  @Before
  public void removeWorkingDirectory() throws IOException {
    FileUtils.deleteDirectory(new File(".jcunit"));
  }

  @Override
  protected Form createForm() {
    // TODO Auto-generated method stub
    return null;
  }
}
