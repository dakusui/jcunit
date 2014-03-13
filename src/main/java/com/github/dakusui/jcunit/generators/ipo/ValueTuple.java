package com.github.dakusui.jcunit.generators.ipo;

import java.util.HashMap;
import java.util.List;

public class ValueTuple extends HashMap<Integer, Object> {
  public static class Attr {
    int attributeIndex;
    Object value;

    public Attr() {
    }

    public Attr(int attributeIndex, Object value) {
      this.attributeIndex = attributeIndex;
      this.value = value;
    }
  }

  public static class ValueTriple extends ValueTuple {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -8146241361960847693L;

    public ValueTriple(Attr a1, Attr a2, Attr a3) {
      super(new Attr[] { a1, a2, a3 });
    }

    public ValueTriple(List<Attr> attrs) {
      super(attrs.get(0), attrs.get(1), attrs.get(2));
      if (attrs.size() != 3)
        throw new IllegalArgumentException();
    }
  }

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6285756323807101173L;

  public ValueTuple(Attr... attrs) {
    for (Attr cur : attrs)
      this.put(cur.attributeIndex, cur.value);
  }
}
