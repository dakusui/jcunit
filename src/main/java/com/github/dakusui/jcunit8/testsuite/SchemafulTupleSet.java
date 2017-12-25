package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.check;
import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;
import static java.util.Collections.*;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface SchemafulTupleSet extends List<Tuple> {
  List<String> getAttributeNames();

  int width();

  SchemafulTupleSet project(List<String> attributes);

  Index index();

  interface Index {
    boolean hasAttribute(String attr);

    List<Tuple> find(Tuple query);

    List<Tuple> allPossibleTuples(List<String> attrs);

    List<Object> getAttributeValuesOf(String attr);

    class Impl implements Index {
      class Pair {
        final String key;
        final Object value;

        Pair(String key, Object value) {
          this.key = key;
          this.value = value;
        }

        @Override
        public final int hashCode() {
          return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public final boolean equals(Object o) {
          if (o == this)
            return true;
          if (o instanceof Pair) {
            Pair e = (Pair) o;
            return Objects.equals(key, e.key) && Objects.equals(value, e.value);
          }
          return false;
        }
      }

      private final Map<String, List<Object>>      attributes;
      private final Map<Pair, List<Tuple>>         cache;
      private final Map<List<String>, List<Tuple>> allPossibleTuplesForAttributes;
      private final SchemafulTupleSet              tupleSet;
      private final List<String>                   attributeNames;

      Impl(SchemafulTupleSet tupleSet) {
        this.tupleSet = checknotnull(tupleSet);
        this.cache = new HashMap<>();
        this.attributes = unmodifiableMap(tupleSet.getAttributeNames().stream().collect(toMap(
            (String s) -> s,
            (String s) -> tupleSet.stream().map(t -> t.get(s)).distinct().collect(toList())
        )));
        this.attributeNames = unmodifiableList(sortAttributeNames(tupleSet.getAttributeNames(), this.attributes));
        allPossibleTuplesForAttributes = new HashMap<>();
      }

      @Override
      public boolean hasAttribute(String attr) {
        return this.attributes.containsKey(attr);
      }

      @Override
      public List<Tuple> find(Tuple query) {
        checknotnull(query);
        if (query.isEmpty())
          return tupleSet;
        if (query.size() == 1)
          return find(query.keySet().iterator().next(), query.values().iterator().next());
        List<List<Tuple>> work = new ArrayList<>(query.size());
        for (String k : query.keySet()) {
          List<Tuple> cur = find(k, query.get(k));
          if (cur.isEmpty())
            return emptyList();
          work.add(cur);
        }
        return work.stream()
            .sorted(comparingInt(List::size))
            .distinct()
            .reduce(this::intersect)
            .orElse(emptyList());
      }

      @Override
      public List<Tuple> allPossibleTuples(List<String> attrs) {
        if (!allPossibleTuplesForAttributes.containsKey(attrs)) {
          allPossibleTuplesForAttributes.put(attrs, tupleSet.stream().map(t -> TupleUtils.project(t, attrs)).distinct().collect(toList()));
        }
        return allPossibleTuplesForAttributes.get(attrs);
      }

      @Override
      public List<Object> getAttributeValuesOf(String attr) {
        return attributes.get(check(attr, attributes::containsKey, () -> String.format("Unknown attribute: '%s'", attr)));
      }


      private <T> List<T> intersect(List<T> a, List<T> b) {
        if (a.isEmpty() || b.isEmpty())
          return emptyList();
        List<T> lhs;
        Set<T> rhs;
        if (a.size() > b.size()) {
          lhs = a;
          rhs = new HashSet<>(b);
        } else {
          lhs = b;
          rhs = new HashSet<>(a);
        }
        return lhs.stream().filter(rhs::contains).collect(toList());
      }

      private List<Tuple> find(String attr, Object value) {
        Pair pair = new Pair(attr, value);
        if (!cache.containsKey(pair)) {
          cache.put(pair, tupleSet.stream()
              .filter(t -> t.containsKey(attr))
              .filter(t -> Objects.equals(t.get(attr), value))
              .collect(toList()));
        }
        return cache.get(pair);
      }

      private static List<String> sortAttributeNames(List<String> attributeNames, Map<String, List<Object>> values) {
        return attributeNames.stream().sorted(
            comparingInt(o -> values.get(o).size())
        ).collect(toList());
      }
    }
  }

  /**
   * Returns all t-way tuples in this {@code SchemafulTupleSet} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   * @return A set of sub-tuples of this.
   */
  TupleSet subtuplesOf(int strength);

  static SchemafulTupleSet fromTuples(List<Tuple> tuples_) {
    Objects.requireNonNull(tuples_);
    FrameworkException.check(tuples_, tuples -> !tuples.isEmpty());
    return new Builder(new ArrayList<>(tuples_.get(0).keySet()))
        .addAll(tuples_)
        .build();
  }

  static SchemafulTupleSet empty(List<String> attributeNames) {
    return new Builder(attributeNames).build();
  }

  class Builder {
    private final LinkedHashSet<String> attributeNames;
    private final List<Tuple>           tuples;

    public Builder(List<String> attributeNames) {
      this.attributeNames = new LinkedHashSet<String>() {{
        addAll(attributeNames);
      }};
      this.tuples = new LinkedList<>();
    }

    public Builder add(Tuple tuple) {
      ////
      // Make sure all the tuples in this suite object have the same set of attribute
      // names.
      FrameworkException.check(tuple, (Tuple t) -> attributeNames.equals(tuple.keySet()), () -> String.format("Allowed keys=%s; given=%s", attributeNames, tuple.keySet()));
      this.tuples.add(tuple);
      return this;
    }

    public Builder addAll(List<Tuple> tuples) {
      tuples.forEach(this::add);
      return this;
    }

    public SchemafulTupleSet build() {
      class Impl extends AbstractList<Tuple> implements SchemafulTupleSet {
        private final List<Tuple>  tuples;
        private final List<String> attributeNames;
        private Index index = null;

        private Impl(List<String> attributeNames, List<Tuple> tuples) {
          this.tuples = unmodifiableList(tuples);
          this.attributeNames = attributeNames;
        }

        @Override
        public Tuple get(int index) {
          return tuples.get(index);
        }

        @Override
        public int size() {
          return tuples.size();
        }

        @Override
        public List<String> getAttributeNames() {
          return attributeNames;
        }

        @Override
        public int width() {
          return getAttributeNames().size();
        }

        @Override
        public SchemafulTupleSet project(List<String> attributes) {
          checknotnull(
              attributes
          );
          check(
              attributes,
              this.attributeNames::containsAll,
              () -> String.format(
                  "Unknown attributes are found: %s",
                  attributes.stream().filter(
                      a -> !this.attributeNames.contains(a)
                  ).collect(toList())
              ));
          return new SchemafulTupleSet.Builder(attributes).addAll(
              this.tuples.stream().map(
                  t -> TupleUtils.project(t, attributes)
              ).distinct(
              ).collect(toList())
          ).build();
        }

        @Override
        public Index index() {
          if (index == null)
            index = new Index.Impl(this);
          return index;
        }

        @Override
        public TupleSet subtuplesOf(int strength) {
          checkIfStrengthIsInRange(strength, attributeNames);
          TupleSet.Builder builder = new TupleSet.Builder();
          for (Tuple each : this) {
            builder.addAll(TupleUtils.subtuplesOf(each, strength));
          }
          return builder.build();
        }

      }
      return new Impl(
          new ArrayList<>(this.attributeNames),
          this.tuples);
    }
  }
}
