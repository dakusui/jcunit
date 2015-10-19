package com.github.dakusui.jcunit.tests.plugins;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
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
public class LevelsProviderTestBase extends BehaviourTestBase<DefaultLevelsProvider> {
  public LevelsProviderTestBase(
      TestScenario.Given<DefaultLevelsProvider> given,
      TestScenario.When<DefaultLevelsProvider> when,
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
                DefaultLevelsProvider ret = new DefaultLevelsProvider();
                ret.init(new Param.ArrayBuilder().build());
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
