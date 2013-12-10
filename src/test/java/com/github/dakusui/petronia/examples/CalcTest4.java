package com.github.dakusui.petronia.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.Summarizer;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;
import com.github.dakusui.petronia.examples.Calc.Op;

@RunWith(JCUnit.class)
public class CalcTest4 extends DefaultRuleSetBuilder {
	static enum TestMode {
		Store,
		Run
	}
	
	@Rule
	public TestName name = new TestName();
	
	@In
	public int a;
	
	@In
	public int b;
	
	@In
	public Op op;

	@Out
	public int r;

	@Out
	public int s;

	@Out
	public Throwable t;
	
	@Rule
	public RuleSet rules = ruleSet()
		.incase(any()).expect(any())
		.summarizer(summarizer);

	@ClassRule
	public static Summarizer summarizer = new BasicSummarizer();

	@Test
	public void test() {
		try {
			Calc calc = new Calc();
			r = calc.calc(op, a, b);
		} catch (RuntimeException e) {
			t = e;
		}
	}
	
	@BeforeClass
	public static void setUpClass() throws JCUnitException {
		if (TestMode.Store == testMode()) {
			createBaseDir();
		}
	}

	@After
	public void verifyValues() throws JCUnitException {
		TestMode testMode = testMode();
		File baseDir = getBaseDir();
		Field[] outFields = Utils.getOutFieldsFromClassUnderTest(this.getClass());
		if (TestMode.Store == testMode) {
			for (Field out : outFields) {
				storeField(baseDir, out);
			}
		} else if (TestMode.Run == testMode){
			for (Field out : outFields) {
				try {
					Assert.assertEquals(loadField(baseDir, out), out.get(this));
				} catch (IllegalArgumentException e) {
					String msg = "";
					throw new JCUnitRuntimeException(msg, e);
				} catch (IllegalAccessException e) {
					String msg = "";
					throw new JCUnitRuntimeException(msg, e);
				}
			}
		} else {
			String msg = String.format("Unsupported mode '%s' is specified.", testMode);
			throw new JCUnitRuntimeException(msg, null);
		}
	}
	
	@AfterClass
	public static void failIfTestMode() {
		////
		// If the test mode is set to 'Store', a test class is made fail in order
		// to avoid misunderstanding that the test class is passing in spite that
		// it is just recording outputs.
		if (TestMode.Store == testMode()) {
			String msg = "This test class fails because it is in 'Store' mode.";
			Assert.fail(msg);
		}
	}

	private static File getBaseDir() {
		return new File(".jcunit");
	}


	private static File createBaseDir() throws JCUnitException {
		File ret = getBaseDir();
		////
		// remove existing directory recursively
		if (ret.exists()) remove(ret);
		
		////
		// Create
		if (!ret.mkdirs()) {
			throw new JCUnitException();
		}
		
		return ret;
	}
	
    private static void remove(File f) throws JCUnitException {
    	if (f.isDirectory()) {
    		for (File child : f.listFiles()) {
    			remove(child);
    		}
    	}
		if (!f.delete()) {
			throw new JCUnitException();
		}
	}

	private void storeField(File baseDir, Field out)  {
		Object value;
		try {
			value = out.get(this);
			saveObjectToFile(fileForField(baseDir, out), value);
		} catch (IllegalArgumentException e) {
			throw new JCUnitRuntimeException(null, e);
		} catch (IllegalAccessException e) {
			throw new JCUnitRuntimeException(null, e);
		}
	}
	
	private Object loadField(File baseDir, Field out) {
		Object ret = readObjectFromFile(fileForField(baseDir, out));
		return ret;
	}

	private File fileForField(File baseDir, Field out) {
		return new File(baseDir, this.getClass().getCanonicalName() + "/" + getTestName() + "/" + out.getName());
	}
	
	private void saveObjectToFile(File f, Object value) {
		try {
			if (!f.getParentFile().isDirectory() && !f.getParentFile().mkdirs()) {
				String msg = String.format("Failed to create a directory '%s'", f.getParentFile());
				throw new JCUnitRuntimeException(msg, null);
			}
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			try {
				oos.writeObject(value);
			} finally {
				oos.close();
			}
		} catch (FileNotFoundException e) {
			String msg = String.format("Failed to find a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		} catch (IOException e) {
			String msg = String.format("Failed to write object (%s) to a file (%s)", f, value);
			throw new JCUnitRuntimeException(msg, e);
		}
	}

	private Object readObjectFromFile(File f) {
		Object ret;
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			try {
				ret = ois.readObject();
			} finally {
				ois.close();
			}
		} catch (ClassNotFoundException e) {
			String msg = String.format("Failed to deserialize an object in a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		} catch (FileNotFoundException e) {
			String msg = String.format("Failed to find a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		} catch (IOException e) {
			String msg = String.format("Failed to read object from a file (%s)", f);
			throw new JCUnitRuntimeException(msg, e);
		}
		return ret;
	}
	
	private static TestMode testMode() {
		if ("store".equals(System.getProperty("jcunit.autotestmode"))) {
			return TestMode.Store;
		}
		return TestMode.Run;
	}
	
	private String getTestName() {
		return this.name.getMethodName();
	}
}
