package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import org.hamcrest.Matcher;

/**
 * An interface that models checking process for an output of a method
 * (a returned value/thrown exception).
 */
public interface OutputChecker {
  OutputType getType();

  abstract class Base implements OutputChecker {
    public final OutputType type;

    public Base(OutputType type) {
      this.type = Checks.checknotnull(type);
    }

    @Override
    public OutputType getType() {
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
    public MatcherBased(OutputType type, Matcher matcher) {
      super(type);
      this.matcher = Checks.checknotnull(matcher);
    }
  }
}
