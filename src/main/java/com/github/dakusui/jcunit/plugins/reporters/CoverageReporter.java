package com.github.dakusui.jcunit.plugins.reporters;

import com.github.dakusui.jcunit.core.CoreBuilder;
import com.github.dakusui.jcunit.plugins.Plugin;

public interface CoverageReporter extends Plugin {
  class Builder implements CoreBuilder<CoverageReporter> {
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
        @Param(source = Param.Source.INSTANCE) int source
    ) {

    }

  }
}
