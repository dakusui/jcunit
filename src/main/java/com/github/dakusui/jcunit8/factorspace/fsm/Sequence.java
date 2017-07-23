package com.github.dakusui.jcunit8.factorspace.fsm;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

public interface Sequence<SUT> extends List<Edge<SUT>>, Stimulus<SUT> {
  class Impl<SUT> extends AbstractList<Edge<SUT>> implements Sequence<SUT> {
    private final List<Edge<SUT>> edges;

    Impl(List<Edge<SUT>> edges) {
      this.edges = edges;
    }

    @Override
    public Edge<SUT> get(int index) {
      return edges.get(index);
    }

    @Override
    public int size() {
      return edges.size();
    }

    @Override
    public void accept(Player<SUT> player) {
      player.visit(this);
    }
  }

  class Builder<SUT> {
    protected final LinkedList<Edge<SUT>> edges;

    public Builder() {
      this.edges = new LinkedList<>();
    }

    public Builder<SUT> add(Edge<SUT> edge) {
      this.edges.add(edge);
      return this;
    }

    public Builder<SUT> addAll(List<Edge<SUT>> edges) {
      Builder.this.edges.addAll(edges);
      return this;
    }

    public Sequence<SUT> build() {
      return new Impl<>(this.edges);
    }

  }
}
