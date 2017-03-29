package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.plugins.levelsproviders.MappingLevelsProviderBase;

public class FSMLevelsProvider<SUT, SPEC extends Enum<? extends StateChecker<SUT>> & StateChecker<SUT>> extends MappingLevelsProviderBase<Story<SUT, SPEC>> {
  private final int historyLength;

  public FSMLevelsProvider(@Param(source = Param.Source.CONFIG, defaultValue = "2") int historyLength) {
    Checks.checkcond(historyLength > 0);
    this.historyLength = historyLength;
  }

  public int historyLength() {
    return this.historyLength;
  }
}
