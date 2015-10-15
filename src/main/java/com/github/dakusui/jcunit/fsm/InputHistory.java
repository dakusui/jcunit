package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.*;

/**
 * Represents a history of inputs to FSM.
 */
public interface InputHistory {
  <T> void add(String name, T data);

  boolean has(String name);

  <E> Record<E> get(String name) throws UndefinedSymbol;

  Iterable<String> recordNames();

  int size();

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
    public <E> Record<E> get(String name) throws UndefinedSymbol {
      if (!this.has(name)) throw new UndefinedSymbol(new String[]{name});
      //noinspection unchecked
      return (Record<E>) this.records.get(name);
    }

    public Iterable<String> recordNames() {
      return records.keySet();
    }

    @Override
    public int size() {
      return this.records.size();
    }
  }

  class Record<T> implements Iterable<T> {

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

  interface Collector {
    void apply(InputHistory inputHistory, Object[] args);

    /**
     * A default argument collector. This accumulate each argument value respectively.
     * You can get each parameter's history by following code.
     * <pre>
     * Record r = inputHistory.get("{actionName}@param-{index}");
     * </pre>
     * where actionName is a value passed to the constructor of this class and
     * index is an index that specifies each element in {@code args}.
     *
     */
    class Default implements Collector {
      private final String actionName;

      public Default(String actionName) {
        this.actionName = Checks.checknotnull(actionName);
      }

      @Override
      public void apply(InputHistory inputHistory, Object[] args) {
        int i = 0;
        for (Object each : args) {
          inputHistory.add(name(i++), each);
        }
      }

      private String name(int i) {
        return String.format("%s@param-%d", actionName, i);
      }
    }
  }

  class CollectorHolder<T extends CollectorHolder> {
    protected final List<InputHistory.Collector> collectors = new LinkedList<InputHistory.Collector>();

    public T resetCollectors() {
      this.collectors.clear();
      //noinspection unchecked
      return (T) this;
    }

    public T addCollector(InputHistory.Collector collector) {
      this.collectors.add(Checks.checknotnull(collector));
      //noinspection unchecked
      return (T) this;
    }
  }

}
