package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.CartesianEnumeratorAdaptor;
import com.github.dakusui.combinatoradix.Domains;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public enum FactorUtils {
  ;

  public static List<Tuple> sortStably(List<Tuple> tuples, final List<Factor> factors) {
    sort(tuples, new Comparator<Tuple>() {
      @Override
      public int compare(Tuple o1, Tuple o2) {
        for (Factor each : factors) {
          boolean o1HasKey = o1.containsKey(each.name);
          boolean o2HasKey = o2.containsKey(each.name);
          if (o1HasKey && o2HasKey) {
            int ret = indexOf(o1.get(each.name), each) - indexOf(o2.get(each.name), each);
            if (ret != 0) {
              return ret;
            }
          } else if (o1HasKey) {
            return -1;
          } else if (o2HasKey) {
            return 1;
          }
        }
        return 0;
      }

      int indexOf(Object o, Factor factor) {
        int i = 0;
        for (Object level : factor.levels) {
          if (Utils.eq(o, level)) {
            return i;
          }
          i++;
        }
        throw Checks.fail();
      }
    });
    return tuples;
  }

  /**
   * Returns cartesian product of given factors. If {@code base} is not empty,
   * Levels of factors not listed in given {@code factors} will be taken from
   * {@code base}.
   *
   * @param base    Gives levels not listed in {@code factors}.
   * @param factors Factors from which returned cartesian product will be calculated.
   */
  public static CartesianTuples enumerateCartesianProduct(final Tuple base, Factor... factors) {
    Checks.checknotnull(base);
    return new CartesianTuples(base, factors);
  }

  public static class CartesianTuples extends CartesianEnumeratorAdaptor<Tuple, String, Object> {

    private final Tuple base;

    protected CartesianTuples(Tuple base, final Factor... factors) {
      super(new Domains<String, Object>() {
        @Override
        public List<String> getDomainNames() {
          List<String> ret = new ArrayList<String>(factors.length);
          for (Factor f : factors) {
            ret.add(f.name);
          }
          return ret;
        }

        @Override
        public List<Object> getDomain(String s) {
          Checks.checknotnull(s);
          for (Factor f : factors) {
            if (s.equals(f.name)) {
              return f.levels;
            }
          }
          return null;
        }
      });
      this.base = Checks.checknotnull(base);
    }

    @Override
    protected Tuple createMap() {
      return base.cloneTuple();
    }
  }
}
