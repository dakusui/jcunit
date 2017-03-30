package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface SchemafulTupleSet extends List<Tuple> {
  List<String> getAttributeNames();

  /**
   * Returns all t-way tuples in this {@code SchemafulTupleSet} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   */
  TupleSet subtuplesOf(int strength);

  SchemafulTupleSet project(List<String> keys);

  static SchemafulTupleSet fromTuples(List<Tuple> tuples_) {
    List<Tuple> tuples = unmodifiableList(Utils.unique(tuples_));
    final List<String> attributeNames = tuples.isEmpty() ?
        emptyList() :
        unmodifiableList(new ArrayList<>(tuples.get(0).keySet()));
    ////
    // Make sure all the tuples in this suite object have the same set of attribute
    // names.
    tuples.forEach(tuple -> {
      assert attributeNames.equals(new ArrayList<>(tuple.keySet()));
    });

    class Impl extends AbstractList<Tuple> implements SchemafulTupleSet {
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
      public TupleSet subtuplesOf(int strength) {
        checkIfStrengthIsInRange(strength, attributeNames);
        TupleSet.Builder builder = new TupleSet.Builder();
        for (Tuple each : this) {
          builder.addAll(TupleUtils.subtuplesOf(each, strength));
        }
        return builder.build();
      }

      @Override
      public SchemafulTupleSet project(List<String> keys) {
        return fromTuples(this.stream().map(tuple -> Utils.project(keys, tuple)).collect(Collectors.toList()));
      }
    }
    return new Impl();
  }

}
