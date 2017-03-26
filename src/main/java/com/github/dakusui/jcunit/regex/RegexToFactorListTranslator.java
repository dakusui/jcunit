package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.framework.TestSuite;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Utils.concatenate;
import static com.github.dakusui.jcunit.core.utils.Utils.filter;
import static java.lang.String.format;

public class RegexToFactorListTranslator extends RegexTranslator {

  public RegexToFactorListTranslator(String prefix, Expr topLevelExpression) {
    super(topLevelExpression, prefix);
  }

  public Factors buildFactors() {
    this.topLevelExpression.accept(this);
    final Factors.Builder builder = new Factors.Builder();
    for (String eachKey : this.terms.keySet()) {
      Factor.Builder b = new Factor.Builder(eachKey);
      if (!isTopLevel(eachKey)) {
        if (isReferencedByAltDirectlyOrIndirectly(eachKey) || isAlt(eachKey)) {
          b.addLevel(VOID);
        }
      }
      if (isAlt(eachKey)) {
        for (Value eachValue : this.terms.get(eachKey)) {
          b.addLevel(resolveIfImmediate(eachValue));
          //          b.addLevel(this.resolve(eachValue));
        }
      } else /* , that is, if (isCat(eachKey)) */ {
        List<Object> work = new LinkedList<Object>();
        for (Value eachValue : this.terms.get(eachKey)) {
          work.addAll(this.resolve(eachValue));
        }
        b.addLevel(work);
      }
      if (b.size() > 1 || (b.size() == 1 && isTopLevel(eachKey))) {
        builder.add(b.build());
      }
    }
    return builder.build();
  }

  public List<TestSuite.Predicate> buildConstraints(List<Factor> factors) {
    List<TestSuite.Predicate> ret = new LinkedList<TestSuite.Predicate>();
    for (final Factor each : factors) {
      final List<String> referrers = Utils.transform(getReferringFactors(each, factors), new Utils.Form<Factor, String>() {
        @Override
        public String apply(Factor in) {
          return in.name;
        }
      });
      if (referrers.isEmpty())
        continue;
      final String referee = each.name;
      final String tag = format("constraint(%s->%s)", referrers, referee);
      ret.add(new TestSuite.Predicate(
          tag,
          concatenate(referrers, referee).toArray(new String[referrers.size() + 1])) {
        @Override
        public boolean apply(Tuple in) {
          for (String eachReferrer : referrers) {
            Object referrerValue = in.get(eachReferrer);
            if (!VOID.equals(referrerValue) && !filter(((List) referrerValue), new Utils.Predicate() {
              @Override
              public boolean apply(Object in) {
                return in instanceof Reference && ((Reference) in).key.equals(referee);
              }
            }).isEmpty()) {
              return !VOID.equals(in.get(referee));
            }
          }
          return VOID.equals(in.get(referee));
        }
      });
    }
    return ret;
  }

  private List<Factor> getReferringFactors(Factor referred, List<Factor> factors) {
    List<Factor> ret = new LinkedList<Factor>();
    outer:
    for (Factor each : factors) {
      if (each == referred)
        continue;
      for (Object eachLevel : each.levels) {
        if (eachLevel instanceof List) {
          for (Object eachElement : (List) eachLevel) {
            if (eachElement instanceof Reference) {
              if (referred.name.equals(((Reference) eachElement).key)) {
                ret.add(each);
                continue outer;
              }
            }
          }
        }
      }
    }
    return ret;
  }
}
