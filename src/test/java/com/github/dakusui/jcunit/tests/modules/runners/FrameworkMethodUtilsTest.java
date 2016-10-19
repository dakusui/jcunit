package com.github.dakusui.jcunit.tests.modules.runners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FrameworkMethodUtilsTest {
  @Test
  public void test() throws Throwable {
    FrameworkMethod method = new FrameworkMethodUtils.JCUnitFrameworkMethod.FromConstraintChecker(createCC(
        new UTUtils.MapBuilder<String, Boolean>()
        .add("testTag", true)
        .build()
    )).buildWith("testTag");
    assertEquals(false, method.invokeExplosively(this));
  }

  static ConstraintChecker createCC(final Map<String, Boolean> values) {
    return new ConstraintChecker.Base() {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<Tuple> getViolations(Tuple regularTestCase) {
        throw new UnsupportedOperationException();
      }

      @Override
      public List<String> getTags() {
        return new LinkedList<String>(values.keySet());
      }

      @Override
      public boolean violates(Tuple tuple, String constraintTag) {
        return values.get(constraintTag);
      }
    };
  }
}
