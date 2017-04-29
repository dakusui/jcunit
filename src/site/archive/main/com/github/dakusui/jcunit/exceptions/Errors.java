package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Errors extends LinkedList<String> implements List<String> {
  public static class Builder {
    private final List<String> messages;

    public Builder() {
      this.messages = new LinkedList<String>();
    }

    public Builder add(String fmt, Object... args) {
      Checks.checknotnull(fmt);
      this.messages.add(StringUtils.format(fmt, args));
      return this;
    }

    public Errors build() {
      return new Errors(messages);
    }
  }

  protected Errors(List<String> messages) {
    super(Collections.unmodifiableList(messages));
  }
}
