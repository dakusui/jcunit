package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public interface Parameters extends Iterable<Factor> {
  Parameters EMPTY = new Parameters() {
    @Override
    public List<List> values() {
      return Collections.emptyList();
    }

    @Override
    public int size() {
      return values().size();
    }

    @Override
    public Factor get(int i) {
      throw new NoSuchElementException();
    }

    @Override
    public Iterator<Factor> iterator() {
      return Collections.<Factor>emptyList().iterator();
    }
  };

  List<List> values();

  int size();

  Factor get(int i);

  class Builder {
    private final String name;
    private List<Factor> parameters = new LinkedList<>();

    public Builder(String name) {
      this.name = requireNonNull(name);
    }

    public Builder add(Object... levels) {
      parameters.add(Factor.create(String.format("%s-p%d", this.name, parameters.size()), levels));
      return this;
    }

    public Parameters build() {
      return new Parameters() {
        @Override
        public Iterator<Factor> iterator() {
          return parameters.iterator();
        }

        @Override
        public List<List> values() {
          return parameters.stream().map(Factor::getLevels).collect(Collectors.toList());
        }

        @Override
        public int size() {
          return parameters.size();
        }

        @Override
        public Factor get(int i) {
          return parameters.get(i);
        }
      };
    }
  }
}
