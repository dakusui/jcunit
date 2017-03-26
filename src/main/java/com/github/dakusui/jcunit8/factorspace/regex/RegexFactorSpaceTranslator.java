package com.github.dakusui.jcunit8.factorspace.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Reference;
import com.github.dakusui.jcunit.regex.RegexTranslator;
import com.github.dakusui.jcunit.regex.Value;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Utils.concatenate;

public class RegexFactorSpaceTranslator extends RegexTranslator {
  public RegexFactorSpaceTranslator(String name, Expr topLevel) {
    super(topLevel, name);
  }

  public FactorSpace buildFactorSpace() {
    List<? extends Factor> factors = buildFactors();
    return FactorSpace.create(factors, buildConstraints(factors));
  }

  private List<? extends Factor> buildFactors() {
    this.topLevelExpression.accept(this);
    final List<Factor> builder = new LinkedList<>();
    for (String eachKey : this.terms.keySet()) {
      List<Object> b = new LinkedList<>();
      if (!isTopLevel(eachKey)) {
        if (isReferencedByAltDirectlyOrIndirectly(eachKey) || isAlt(eachKey)) {
          b.add(VOID);
        }
      }
      if (isAlt(eachKey)) {
        for (Value eachValue : this.terms.get(eachKey)) {
          b.add(resolveIfImmediate(eachValue));
        }
      } else /* , that is, if (isCat(eachKey)) */ {
        List<Object> work = new LinkedList<>();
        for (Value eachValue : this.terms.get(eachKey)) {
          work.addAll(this.resolve(eachValue));
        }
        b.add(work);
      }
      if (b.size() > 1 || (b.size() == 1 && isTopLevel(eachKey))) {
        builder.add(Factor.create(eachKey, b.toArray()));
      }
    }
    return Collections.unmodifiableList(builder);
  }

  private List<Constraint> buildConstraints(List<? extends Factor> factors) {
    List<Constraint> ret = new LinkedList<>();
    for (final Factor each : factors) {
      final List<String> referrers = Utils.transform(getReferringFactors(each, factors), new Utils.Form<Factor, String>() {
        @Override
        public String apply(Factor in) {
          return in.getName();
        }
      });
      if (referrers.isEmpty())
        continue;
      final String referee = each.getName();
      final String tag = String.format("constraint(%s->%s)", referrers, referee);
      ret.add(new Constraint() {

        @Override
        public boolean test(Tuple in) {
          for (String eachReferrer : referrers) {
            Object referrerValue = in.get(eachReferrer);
            if (!VOID.equals(referrerValue) && isReferencedBy(referrerValue)) {
              return !VOID.equals(in.get(referee));
            }
          }
          return VOID.equals(in.get(referee));
        }

        @Override
        public List<String> involvedKeys() {
          return concatenate(referrers, referee);
        }

        @Override
        public String toString() {
          return tag;
        }

        boolean isReferencedBy(Object referrerValue) {
          //noinspection unchecked
          return ((List<Object>) referrerValue).stream()
              .anyMatch(
                  (Object each) ->
                      each instanceof Reference &&
                          ((Reference) each).key.equals(referee)
              );
        }
      });
    }
    return ret;
  }

  private List<Factor> getReferringFactors(Factor referred, List<? extends Factor> factors) {
    List<Factor> ret = new LinkedList<>();
    outer:
    for (Factor each : factors) {
      if (each == referred)
        continue;
      for (Object eachLevel : each.getLevels()) {
        if (eachLevel instanceof List) {
          for (Object eachElement : (List) eachLevel) {
            if (eachElement instanceof Reference) {
              if (referred.getName().equals(((Reference) eachElement).key)) {
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
