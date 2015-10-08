package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.*;

/**
 * Represents a history of inputs to FSM.
 */
public interface InputHistory {
  <T> void add(String name, T data);

  boolean has(String name);

  <E> Record<E> get(String name);

  Iterable<String> recordNames();

  class Base implements InputHistory {
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
    public boolean has(String name) {
      return this.records.containsKey(Checks.checknotnull(name));
    }

    @Override
    public <E> Record<E> get(String name) {
      Checks.checkcond(this.has(name), "Unknown input argument name '%s' is specified.", name);
      //noinspection unchecked
      return (Record<E>) this.records.get(name);
    }

    public Iterable<String> recordNames() {
      return records.keySet();
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder(String.format("%s:{", this.getClass().getSimpleName()));
      boolean firstTime = true;
      for (String each: this.recordNames()) {
        if (!firstTime) {
          b.append(",");
        }
        firstTime = false;
        b.append(String.format("%s:%s", each, this.get(each)));
      }
      b.append("}");
      return b.toString();
    }
  }

  class Record<T> implements Iterable<T> {

    public final  String  name;
    private final List<T> items;

    public Record(String name) {
      this.name = Checks.checknotnull(name);
      this.items = new LinkedList<T>();
    }

    public void add(T item) {
      this.items.add(item);
    }

    public int size() {
      return this.items.size();
    }

    @Override
    public Iterator<T> iterator() {
      return this.items.iterator();
    }

    public String toString() {
      return this.items.toString();
    }
  }

  interface Collector {
    void apply(InputHistory inputHistory, Args args);

    /**
     * A default argument collector. This accumulate each argument value respectively.
     * You can get each parameter's history by
     */
    class Default implements Collector {
      private final String actionName;

      public Default(String actionName) {
        this.actionName = Checks.checknotnull(actionName);
      }

      @Override
      public void apply(InputHistory inputHistory, Args args) {
        int i = 0;
        for (Object each : args.values()) {
          inputHistory.add(name(i++), each);
        }
      }

      private String name(int i) {
        return String.format("%s@param-%d", actionName, i);
      }
    }
  }

}
