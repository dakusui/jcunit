package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.*;

/**
 * Represents a history of interaction with FSM.
 */
public interface InteractionHistory {
  <T> void add(String name, T data);
  void add(Action<?> action, Args args);

  boolean has(String name);

  <E> Record<E> get(String name);

  int size();

  class Base implements InteractionHistory {
    private final Map<String, Record<?>> records = new LinkedHashMap<String, Record<?>>();

    @Override
    public <T> void add(String name, T data) {
      if (!this.records.containsKey(Checks.checknotnull(name))) {
        this.records.put(name, new Record<T>(name));
      }
      //noinspection unchecked
      ((Record<T>) this.records.get(name)).add(data);
    }

    @Override
    public void add(Action<?> action, Args args) {
      {
        String alias = action.getAlias();
        if (alias != null) {
          this.add(alias, args.values());
        }
      }
      Checks.checkcond(action.numParameterFactors() == args.size());
      for (int i = 0; i < action.numParameterFactors(); i++) {
        String alias = action.getAliasForParameter(i);
        if (alias != null) {
          this.add(alias, args.values()[i]);
        }
      }
    }

    @Override
    public boolean has(String name) {
      return this.records.containsKey(Checks.checknotnull(name));
    }

    @Override
    public <E> Record<E> get(String name) {
      if (!this.has(name))
        return Record.empty();
      //noinspection unchecked
      return (Record<E>) this.records.get(name);
    }

    @Override
    public int size() {
      return this.records.size();
    }
  }

  class Record<T> implements Iterable<T> {
    public static final Record<?> EMPTY = new Record<Object>();

    private Record() {
      this.name = null;
      this.items = Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static <T> Record<T> empty(){
      return (Record<T>) EMPTY;
    }

    public final  String  name;
    private final List<T> items;

    public Record(String name) {
      this.name = Checks.checknotnull(name);
      this.items = new ArrayList<T>();
    }

    public void add(T item) {
      this.items.add(item);
    }

    public int size() {
      return this.items.size();
    }

    public T get(int i) {
      return this.items.get(i);
    }

    @Override
    public Iterator<T> iterator() {
      return this.items.iterator();
    }

    public String toString() {
      return this.items.toString();
    }
  }
}
