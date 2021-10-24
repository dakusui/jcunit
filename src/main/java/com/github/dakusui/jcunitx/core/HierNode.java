package com.github.dakusui.jcunitx.core;

import java.util.*;

import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Predicates.isNotNull;

/**
 * An abstract data model for JSON like hierchical notations.
 * Multi-key isn't allowed.
 */
public interface HierNode {
  static HierNode example() {
    return object(
        $("key1", $("hello")),
        $("key2", array($("value1"), $(1), $(2), $(3))));
  }

  static ArrayNode array(HierNode... elements) {
    ArrayNode ret = ArrayNode.create();
    Collections.addAll(ret, elements);
    return ret;
  }

  @SafeVarargs
  static ObjectNode object(KeyValuePair<? extends HierNode>... pairs) {
    ObjectNode ret = ObjectNode.create();
    for (KeyValuePair<? extends HierNode> each : pairs)
      ret.put(each);
    return ret;
  }

  static Text text(String text) {
    return Text.create(text);
  }

  static Numeric numeric(Number number) {
    return Numeric.create(number);
  }

  static Null nullNode() {
    return Null.INSTANCE;
  }

  static <V extends HierNode> KeyValuePair<V> pair(String key, V value) {
    return KeyValuePair.create(key, value);
  }

  static <V extends HierNode> KeyValuePair<V> $(String key, V value) {
    return pair(key, value);
  }

  static Text $(String text) {
    return text(text);
  }

  static Numeric $(Number number) {
    return numeric(number);
  }

  default Text toText() {
    throw new UnsupportedOperationException();
  }

  default Numeric toNumeric() {
    throw new UnsupportedOperationException();
  }

  default ObjectNode toObjectNode() {
    throw new UnsupportedOperationException();
  }

  default ArrayNode toArrayNode() {
    throw new UnsupportedOperationException();
  }

  default Null toNull() {
    throw new UnsupportedOperationException();
  }

  default void accept(HierNodeVisitor visitor) {
    throw new UnsupportedOperationException();
  }

  interface ArrayNode extends HierNode, List<HierNode> {
    static ArrayNode create() {
      class Impl extends LinkedList<HierNode> implements ArrayNode {
      }
      return new Impl();
    }

    @Override
    default ArrayNode toArrayNode() {
      return this;
    }
  }

  interface ObjectNode extends HierNode, Map<String, HierNode> {
    static ObjectNode create() {
      class Impl extends HashMap<String, HierNode> implements ObjectNode {
      }
      return new Impl();
    }

    default HierNode put(KeyValuePair<? extends HierNode> pair) {
      return this.put(pair.key(), pair.value());
    }

    @Override
    default ObjectNode toObjectNode() {
      return this;
    }
  }

  interface Atom extends HierNode {
    Object value();
  }

  interface Text extends Atom {
    static Text create(String value) {
      assert that(value, isNotNull());
      return () -> value;
    }

    String value();

    @Override
    default Text toText() {
      return this;
    }
  }

  interface Numeric extends Atom {
    static Numeric create(Number number) {
      return () -> number;
    }

    Number value();

    @Override
    default Numeric toNumeric() {
      return this;
    }
  }

  interface Null extends Atom {
    Null INSTANCE = () -> null;
  }
}
