package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.Recorder;
import com.github.dakusui.jcunit.generators.Replayer;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = Replayer.class,
        params = @Param("FailedOnly")
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
  public int x;

  @BeforeClass
  public static void beforeClass() {
    Recorder.initializeDir(ReplayerExample.class);
  }

  @Test
  public void testX() {
//    assertTrue(k > 0);
  }

  @Test
  public void testY() {
//    assertTrue(k > 100);
  }

  //@Replayer.CompareWithPrevious
  @Rule
  public MethodRule comp1() {
//    System.out.println("comp1:called");
    return new MethodRule() {
      @Override public Statement apply(Statement base, FrameworkMethod method,
          Object target) {
        return new Statement() {
          @Override public void evaluate() throws Throwable {
            System.out.println("comp1:evaluated");
          }
        };
      }
    };
  }

  @Rule
  public TestRule comp2() {
//    System.out.println("comp2:called");
    return new TestRule() {
      @Override public Statement apply(final Statement base,
          Description description) {
        return new Statement() {
          @Override public void evaluate() throws Throwable {
            System.out.println("comp2:evaluated");
            base.evaluate();
          }
        };
      }
    };
  }

  @After
  public void after() {
    System.out.println("after");
  }

}
