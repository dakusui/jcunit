package com.github.dakusui.jcunit.tests.plugins;

import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProviderBase;
import com.github.dakusui.jcunit.ututils.behaviour.BehaviourTestBase;
import com.github.dakusui.jcunit.ututils.behaviour.TestScenario;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Spec of LevelsProvider implementations.
 * <pre>
 * Sequence:
 *   [setTargetField(Field), setAnnotation(FactorField}] (both)
 *   init(Param[])
 *   {size(), get(int)}*
 * Input:
 *   setTargetField(Field) : void
 *   setField(FactorField) : void
 * Output:
 *   getErrorsOnInitialization() : List<String>
 *   size() : int
 *   get(int) : T
 * </pre>
 */
public class LevelsProviderTestBase<T extends LevelsProvider> extends BehaviourTestBase<T> {
  public LevelsProviderTestBase(
      TestScenario.Given<T> given,
      TestScenario.When<T> when,
      TestScenario.Then then) {
    super(given, when, then);
  }

  @Parameterized.Parameters
  public static Object[][] testScenarios() {
    return new Object[][] {
        {
            new TestScenario.Given<LevelsProvider>() {
              @Override
              public LevelsProvider prepare() {
                // TODO
                LevelsProvider ret = new LevelsProviderBase() {
                  @Override
                  public int size() {
                    return 0;
                  }

                  @Override
                  public Object get(int n) {
                    return null;
                  }
                };
                return ret;
              }
            },
            new TestScenario.When<LevelsProvider>() {
              @Override
              public Object perform(LevelsProvider sut) throws Throwable {
                return sut.size();
              }
            },
            new TestScenario.Then() {
              @Override
              public void assertOutput(TestScenario.Output output) {
                assertEquals(7, output.output);
              }
            }
        }
    };
  }
}
