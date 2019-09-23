package com.github.dakusui.jcunit8.factorspace.regex;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Reference;
import com.github.dakusui.jcunit.regex.RegexTranslator;
import com.github.dakusui.jcunit.regex.Value;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit.core.utils.Utils.concatenate;
import static java.util.Objects.requireNonNull;

public class RegexDecomposer extends RegexTranslator {

  public RegexDecomposer(String name, Expr topLevel) {
    super(topLevel, name);
  }

  public FactorSpace decompose() {
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
          b.add(Generator.VOID);
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
      final List<String> referrers = getReferringFactors(each, factors).stream()
          .map(Factor::getName)
          .collect(Collectors.toList());
      if (referrers.isEmpty())
        continue;
      final String referee = each.getName();
      final String tag = String.format("constraint(%s->%s)", referrers, referee);
      ret.add(new Constraint() {

        @Override
        public String getName() {
          return toString();
        }

        @Override
        public boolean test(Tuple in) {
          for (String eachReferrer : referrers) {
            Object referrerValue = in.get(eachReferrer);
            if (!Generator.VOID.equals(referrerValue) && isReferencedBy(referrerValue)) {
              return !Generator.VOID.equals(in.get(referee));
            }
          }
          return Generator.VOID.equals(in.get(referee));
        }

        @Override
        public List<String> involvedKeys() {
          return concatenate(referrers, referee);
        }

        @Override
        public String toString() {
          return tag;
        }

        @SuppressWarnings("unchecked")
        boolean isReferencedBy(Object referrerValue) {
          return ((List<Object>) requireNonNull(referrerValue)).stream()
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
