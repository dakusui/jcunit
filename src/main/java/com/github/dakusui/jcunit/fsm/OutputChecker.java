package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import org.hamcrest.Matcher;

/**
 * An interface that models checking process for an output of a method
 * (a returned value/thrown exception).
 */
public interface OutputChecker {
  Output.Type getType();

  abstract class Base implements OutputChecker {
    public final Output.Type type;

    public Base(Output.Type type) {
      this.type = Checks.checknotnull(type);
    }

    @Override
    public Output.Type getType() {
      return this.type;
    }
  }

  class MatcherBased extends Base implements OutputChecker {
    private final Matcher matcher;

    /**
     * Creates an object of this class.
     *
     * @param type    expected output type.
     * @param matcher expectation for output.
     */
    public MatcherBased(Output.Type type, Matcher matcher) {
      super(type);
      this.matcher = Checks.checknotnull(matcher);
    }
  }
}
