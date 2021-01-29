package com.github.dakusui.peerj.model;

import com.github.dakusui.peerj.ext.base.NormalizableConstraint;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dakusui.peerj.ext.base.ConstraintUtils.*;

public enum ConstraintSet {
  NONE {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.empty();
    }
  },
  /*
    pi,1>pi,2∨pi,3>pi,4∨pi,5>pi,6∨pi,7>pi,8∨pi,9>pi,2(0≤i<n)
   */
  BASIC {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.of(factorNames -> createBasicConstraint(offset).apply(factorNames));
    }
  },
  /*
    (pi,1>pi,2∨pi,3>pi,4∨pi,5>pi,6∨pi,7>pi,8∨pi,9>pi,2)∧pi,10>pi,1∧pi,9>pi,2∧pi,8>pi,3∧pi,7>pi,4∧pi,6>pi,5(0≤i<n)
   */
  BASIC_PLUS {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.of(factorNames -> createBasicPlusConstraint(offset).apply(factorNames));
    }
  };

  /**
   * <pre>
   *     <Constraints>
   *       <Constraint text="l01 &lt;= l02 || l03 &lt;= l04 || l05 &lt;= l06 || l07&lt;= l08 || l09 &lt;= l02">
   *       <Parameters>
   *         <Parameter name="l01" />
   *         <Parameter name="l02" />
   *         <Parameter name="l03" />
   *         <Parameter name="l04" />
   *         <Parameter name="l05" />
   *         <Parameter name="l06" />
   *         <Parameter name="l07" />
   *         <Parameter name="l08" />
   *         <Parameter name="l09" />
   *         <Parameter name="l02" />
   *       </Parameters>
   *     </Constraint>
   *   </Constraints>
   * </pre>
   * <pre>
   *   p i,1 > p i,2 ∨ p i,3 > p i,4 ∨ p i,5 > p i,6 ∨ p i,7 > p i,8 ∨ p i,9 > p i,2
   * </pre>
   *
   * @param factorNames A list of factor names.
   */
  public static NormalizableConstraint createBasicConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return or(
        ge(p[0], p[1]),
        gt(p[2], p[3]),
        eq(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1]));
  }

  /*
        (pi,1>pi,2 ∨ pi,3>pi,4 ∨ pi,5>pi,6 ∨ pi,7>pi,8 ∨ pi,9>pi,2)
                      ∧pi,10>pi,1
                      ∧pi,9>pi,2
                      ∧pi,8>pi,3
                      ∧pi,7>pi,4
                      ∧pi,6>pi,5 (0≤i<n)
     */
  public static NormalizableConstraint createBasicPlusConstraint(List<String> factorNames) {
    String[] p = factorNames.toArray(new String[0]);
    return and(or(
        ge(p[0], p[1]),
        gt(p[2], p[3]),
        eq(p[4], p[5]),
        gt(p[6], p[7]),
        gt(p[8], p[1])), gt(p[9], p[0]), gt(p[8], p[1]), gt(p[7], p[2]), gt(p[6], p[3]), gt(p[5], p[4]));
  }

  public static Function<List<String>, NormalizableConstraint> createBasicConstraint(int offset) {
    return strings -> createBasicConstraint(strings.subList(offset, offset + 10));
  }

  public static Function<List<String>, NormalizableConstraint> createBasicPlusConstraint(int offset) {
    return strings -> createBasicPlusConstraint(strings.subList(offset, offset + 10));
  }

  public abstract Optional<NormalizedConstraintFactory> constraintFactory(int offset);

  public interface NormalizedConstraintFactory extends Function<List<String>, NormalizableConstraint> {
  }
}
