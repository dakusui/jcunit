package com.github.dakusui.jcunitx.testutils;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.runners.core.TestInputPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CustomParameter extends Parameter.Base<CustomParameter.ValuePair> {
  private final ArrayList<String> values;

  public CustomParameter(String name, List<String> values) {
    super(name, emptyList());
    this.values = new ArrayList<>(values);
  }

  @Override
  public ValuePair composeValue(AArray tuple) {
    return new ValuePair(getValue(tuple, "a"), getValue(tuple, "b"));
  }

  @Override
  public Optional<AArray> decomposeValue(ValuePair value) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected List<Factor> decompose() {
    return asList(
        Factor.create(composeKey("a"), this.values.toArray()),
        Factor.create(composeKey("b"), this.values.toArray())
    );
  }

  private String composeKey(String keyName) {
    return String.format("CUSTOM:%s:%s", name, keyName);
  }

  private String getValue(AArray tuple, String keyName) {
    return (String) tuple.get(composeKey(keyName));
  }

  @Override
  protected List<Constraint> generateConstraints() {
    return singletonList(new Constraint() {
      @Override
      public String getName() {
        return CustomParameter.this.toString();
      }

      @Override
      public boolean test(AArray tuple) {
        return !Objects.equals(getValue(tuple, "a"), getValue(tuple, "b"));
      }

      @Override
      public List<String> involvedKeys() {
        return asList(composeKey("a"), composeKey("b"));
      }

      @Override
      public String toString() {
        return TestInputPredicate.toString(this);
      }
    });
  }

  @Override
  public String toString() {
    return String.format("CUSTOM:%s:%s", name, values);
  }

  public static class ValuePair {
    private final String a;
    private final String b;

    ValuePair(String a, String b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public String toString() {
      return String.format("ValuePair(%s,%s)", a, b);
    }
  }
}
