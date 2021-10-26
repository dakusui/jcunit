package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.exceptions.FrameworkException;

import java.util.*;

import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface CoveringArray extends List<Row> {
  static CoveringArray fromRows(List<Row> rows) {
    Objects.requireNonNull(rows);
    FrameworkException.check(rows, tuples -> !tuples.isEmpty());
    return new Builder(new ArrayList<>(rows.get(0).keySet()))
        .addAll(rows)
        .build();
  }

  static CoveringArray empty(List<String> attributeNames) {
    return new Builder(attributeNames).build();
  }

  List<String> getAttributeNames();

  int width();

  /**
   * Returns all t-way tuples in this {@code CoveringArray} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   * @return A set of sub-tuples of this.
   */
  TupleSet subtuplesOf(int strength);

  class Builder {
    private final LinkedHashSet<String> attributeNames;
    private final List<Row>             rows;

    public Builder(List<String> attributeNames) {
      this.attributeNames = new LinkedHashSet<>();
      this.attributeNames.addAll(attributeNames);
      this.rows = new LinkedList<>();
    }

    public Builder add(Row row) {
      ////
      // Make sure all the tuples in this suite object have the same set of attribute
      // names.
      FrameworkException.check(row, (KeyValuePairs t) -> attributeNames.equals(row.keySet()));
      this.rows.add(row);
      return this;
    }

    public Builder addAll(List<Row> rows) {
      rows.forEach(this::add);
      return this;
    }

    public CoveringArray build() {
      class Impl extends AbstractList<Row> implements CoveringArray {
        private final List<Row>    rows;
        private final List<String> attributeNames;

        private Impl(List<String> attributeNames, List<Row> rows) {
          this.rows = rows;
          this.attributeNames = Collections.unmodifiableList(attributeNames);
        }

        @Override
        public Row get(int index) {
          return rows.get(index);
        }

        @Override
        public int size() {
          return rows.size();
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
        public TupleSet subtuplesOf(int strength) {
          checkIfStrengthIsInRange(strength, attributeNames);
          TupleSet.Builder builder = new TupleSet.Builder();
          for (KeyValuePairs each : this) {
            builder.addAll(KeyValuePairsUtils.subtuplesOf(each, strength));
          }
          return builder.build();
        }
      }
      return new Impl(
          new ArrayList<>(this.attributeNames),
          this.rows);
    }
  }

}
