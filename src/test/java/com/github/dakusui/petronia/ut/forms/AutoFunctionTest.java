package com.github.dakusui.petronia.ut.forms;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.github.dakusui.jcunit.core.Out;

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
}
