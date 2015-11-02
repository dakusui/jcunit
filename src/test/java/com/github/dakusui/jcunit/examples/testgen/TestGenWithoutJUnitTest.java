package com.github.dakusui.jcunit.examples.testgen;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class TestGenWithoutJUnitTest {

  public static final String EXPECTATION = String.format(
      "{Bits=32, Browser=Chrome, OS=Windows}%n"
      + "{Bits=64, Browser=Firefox, OS=Windows}%n"
      + "{Bits=64, Browser=Chrome, OS=Linux}%n"
      + "{Bits=32, Browser=Firefox, OS=Linux}%n");

  @Test
  public void testNormally() throws UnsupportedEncodingException {
    String actual = runCAEngineNormally();
    assertEquals(EXPECTATION, actual);
  }

  @Test
  public void testFluently() throws UnsupportedEncodingException {
    String actual = runCAEngineFluently();
    assertEquals(EXPECTATION, actual);
  }

  private String runCAEngineNormally() throws UnsupportedEncodingException {
    return this.runCAEngine(Run.Normally);
  }

  private String runCAEngineFluently() throws UnsupportedEncodingException {
    return this.runCAEngine(Run.Fluently);
  }

  private String runCAEngine(Run run) throws UnsupportedEncodingException {
    ByteArrayOutputStream baos;
    PrintStream ps = new PrintStream(baos = new ByteArrayOutputStream());
    try {
      run.run(new TestGenWithoutJUnit(), ps);
    } finally {
      ps.close();
    }
    return new String(baos.toByteArray(), "UTF-8");
  }

  public enum Run {
    Normally {
      @Override
      public void run(TestGenWithoutJUnit gen, PrintStream ps) {
        gen.run(ps);
      }
    },
    Fluently {
      @Override
      public void run(TestGenWithoutJUnit gen, PrintStream ps) {
        gen.moreFluentStyleRun(ps);
      }
    };

    public abstract void run(TestGenWithoutJUnit gen, PrintStream ps);
  }
}
