package com.github.dakusui.lisj;

import java.io.Serializable;

public class Symbol implements Serializable {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 5209431927035873439L;

  private String name;

  public Symbol(String name) {
    this.name = name;
  }

  public String name() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public final boolean equals(Object another) {
    if (another == null || !(another instanceof Symbol)) {
      return false;
    }
    return this.name.equals(((Symbol) another).name);
  }
}
