package com.github.dakusui.jcunit.plugins.reporters;

import com.github.dakusui.jcunit.core.BaseBuilder;
import com.github.dakusui.jcunit.plugins.Plugin;

public interface CoverageReporter extends Plugin {
  class Builder implements BaseBuilder<CoverageReporter> {
    public Builder() {
    }

    @Override
    public CoverageReporter build() {
      return null;
    }
  }

  abstract class Base implements CoverageReporter {

  }

  class Default extends Base {
    public Default(
    ) {
    }

  }
}
