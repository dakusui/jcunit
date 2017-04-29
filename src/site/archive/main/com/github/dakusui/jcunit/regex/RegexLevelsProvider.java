package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.plugins.levelsproviders.MappingLevelsProviderBase;

public class RegexLevelsProvider extends MappingLevelsProviderBase {
  private final String sequence;

  /**
   * Creates an object of this class.
   *
   * @param sequence A string that represents a sequence of strings in regex (kind of).
   */
  public RegexLevelsProvider(@Param(source = Param.Source.CONFIG) String sequence) {
    this.sequence = sequence;
  }

  public String getSequence() {
    return this.sequence;
  }
}
