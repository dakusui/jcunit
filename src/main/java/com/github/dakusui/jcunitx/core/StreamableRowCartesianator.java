package com.github.dakusui.jcunitx.core;

import com.github.dakusui.combinatoradix.CartesianEnumeratorAdaptor;
import com.github.dakusui.combinatoradix.Domains;
import com.github.dakusui.jcunitx.factorspace.Factor;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A utility class to stream all possible rows from the given factors.
 */
public class StreamableRowCartesianator extends CartesianEnumeratorAdaptor<AArray, String, Object> {
  public StreamableRowCartesianator(List<Factor> factors) {
    super(buildDomains(factors));
  }

  public Cursor<AArray> cursor(AArray at) {
    return new Cursor.ForRow(indexOf(at), this);
  }

  public Stream<AArray> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  public List<AArray> asList() {
    return new AbstractList<AArray>() {
      @Override
      public AArray get(int index) {
        return StreamableRowCartesianator.this.get(index);
      }

      @Override
      public int size() {
        return (int) StreamableRowCartesianator.this.size();
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
