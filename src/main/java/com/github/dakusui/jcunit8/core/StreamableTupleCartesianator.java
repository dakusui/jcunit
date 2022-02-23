package com.github.dakusui.jcunit8.core;

import com.github.dakusui.combinatoradix.CartesianEnumeratorAdaptor;
import com.github.dakusui.combinatoradix.Domains;
import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamableTupleCartesianator extends CartesianEnumeratorAdaptor<AArray, String, Object> {
  public StreamableTupleCartesianator(List<Factor> factors) {
    super(buildDomains(factors));
  }

  public Cursor<AArray> cursor(AArray at) {
    return new Cursor.ForTuple(indexOf(at), this);
  }

  public Stream<AArray> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  public List<AArray> asList() {
    return new AbstractList<AArray>() {
      @Override
      public AArray get(int index) {
        return StreamableTupleCartesianator.this.get(index);
      }

      @Override
      public int size() {
        return (int) StreamableTupleCartesianator.this.size();
      }
    };
  }

  @Override
  protected AArray createMap() {
    return new AArray.Builder().build();
  }

  private static Domains<String, Object> buildDomains(List<Factor> factors) {
    Domains.Builder<String, Object> builder = new Domains.Builder<>();
    factors.forEach(factor -> builder.addDomain(factor.getName(), factor.getLevels().toArray()));
    return builder.build();
  }
}
