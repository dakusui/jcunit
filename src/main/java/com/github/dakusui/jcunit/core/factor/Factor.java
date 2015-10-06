package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Factor implements Iterable<Object> {
  public final String       name;
  public final List<Object> levels;

  public Factor(String name, List<Object> levels) {
    Checks.checknotnull(name, "A factor's 'name' mustn't be null");
    Checks.checknotnull(levels, "A factor's 'levels' mustn't be null(factor:'%s')", name);
    Checks.checkcond(levels.size() > 0, "Factor '%s' has no levels.", name);
    this.name = name;
    this.levels = Collections.unmodifiableList(levels);
  }

  @Override
  public Iterator<Object> iterator() {
    return this.levels.iterator();
  }

  public static class Builder {
    private String name;
    private List<Object> levels = new LinkedList<Object>();

    public Builder(String name) {
      this.name = Checks.checknotnull(name);
    }

    public Builder addLevel(Object level) {
      this.levels.add(level);
      return this;
    }

    public Factor build() {
      return new Factor(this.name, this.levels);
    }

    public String getName() {
      return name;
    }

    public boolean hasLevel(Object v) {
      return this.levels.contains(v);
    }
  }
}
