package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.check;
import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface SchemafulTupleSet extends List<Tuple> {
  List<String> getAttributeNames();

  List<Object> getAttributeValuesOf(String attr);

  int width();

  SchemafulTupleSet project(List<String> attributes);

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
      FrameworkException.check(tuple, (Tuple t) -> attributeNames.equals(tuple.keySet()));
      this.tuples.add(tuple);
      return this;
    }

    public Builder addAll(List<Tuple> tuples) {
      tuples.forEach(this::add);
      return this;
    }

    public SchemafulTupleSet build() {
      class Impl extends AbstractList<Tuple> implements SchemafulTupleSet {
        private final List<Tuple>               tuples;
        private final List<String>              attributeNames;
        private       Map<String, List<Object>> attributes;

        private Impl(List<String> attributeNames, List<Tuple> tuples) {
          this.tuples = unmodifiableList(tuples);
          this.attributes = unmodifiableMap(attributeNames.stream().collect(Collectors.toMap(
              (String s) -> s,
              (String s) -> tuples.stream().map(t -> t.get(s)).distinct().collect(toList())
          )));
          this.attributeNames = unmodifiableList(sortAttributeNames(attributeNames, this.attributes));
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
        public List<Object> getAttributeValuesOf(String attr) {
          return attributes.get(check(attr, attributes::containsKey, () -> String.format("Unknown attribute: '%s'", attr)));
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
              ).collect(toList())
          ).build();
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

    private static List<String> sortAttributeNames(List<String> attributeNames, Map<String, List<Object>> values) {
      return attributeNames.stream().sorted(
          comparingInt(o -> values.get(o).size())
      ).collect(toList());
    }
  }

}
