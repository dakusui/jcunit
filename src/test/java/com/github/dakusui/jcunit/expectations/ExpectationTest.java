package com.github.dakusui.jcunit.expectations;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.ututils.UTUtils;
import com.github.dakusui.jcunit.ututils.tuples.*;
import org.junit.Test;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpectationTest {

  @Test
  public void presence01() throws Exception {
    VerificationResult result = verify(
        PresenceExpectation.class,
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        ),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        )
    );
    assertThat(result.isSuccessful(), is(true));
    result.check();
  }

  @Test(expected = JCUnitAssertionError.class)
  public void presence02() throws Exception {
    VerificationResult result = verify(
        PresenceExpectation.class,
        UTUtils.tuples(

            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        ),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").build()
        ));
    assertThat(result.isSuccessful(), is(false));
    result.check();
  }


  @Test
  public void presence03() throws Exception {
    VerificationResult result = verify(PresenceExpectation.class,
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        ), UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").put("C", "c1").build()
        ));
    assertThat(result.isSuccessful(), is(true));
    result.check();
  }

  @Test
  public void presence04() throws Exception {
    VerificationResult result =
        verify(PresenceExpectation.class,
            UTUtils.tuples(
                UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
            ), UTUtils.tuples(
                UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").put("C", "c1").build(),
                UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").put("C", "c2").build()
            ));
    assertThat(result.isSuccessful(), is(true));
    result.check();
  }

  @Test(expected = JCUnitAssertionError.class)
  public void absence01() throws Exception {
    VerificationResult result = verify(
        AbsenceExpectation.class,
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        ),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        )
    );
    assertThat(result.isSuccessful(), is(false));
    result.check();
  }

  @Test
  public void absence02() throws Exception {
    VerificationResult result = verify(
        AbsenceExpectation.class,
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        ),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build()
        )
    );
    assertThat(result.isSuccessful(), is(true));
    result.check();
  }

  @Test
  public void validTuplesCovered01() throws Exception {
    VerificationResult result = verify(
        new ValidTuplesCoveredExpectation(UTUtils.defaultFactors, 2, ConstraintManager.DEFAULT_CONSTRAINT_MANAGER),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test
  public void validTuplesCovered02() throws Exception {
    ////
    // Even if there is extra tuple, it should be acceptable.
    VerificationResult result = verify(
        new ValidTuplesCoveredExpectation(UTUtils.defaultFactors, 2, ConstraintManager.DEFAULT_CONSTRAINT_MANAGER),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a3").put("B", "b3").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test(expected = JCUnitAssertionError.class)
  public void validTuplesCovered03() throws Exception {
    ////
    // If there is a missing tuple, it will be complained.
    VerificationResult result = verify(
        new ValidTuplesCoveredExpectation(UTUtils.defaultFactors, 2, ConstraintManager.DEFAULT_CONSTRAINT_MANAGER),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b1").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test
  public void sanity01() throws Exception {
    VerificationResult result = verify(
        new SanityExpectation(UTUtils.defaultFactors),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b2").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b1").build(),
            UTUtils.tupleBuilder().put("A", "a2").put("B", "b2").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test
  public void sanity02() throws Exception {
    VerificationResult result = verify(
        new SanityExpectation(UTUtils.defaultFactors),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test(expected = JCUnitAssertionError.class)
  public void sanityE01() throws Exception {
    VerificationResult result = verify(
        new SanityExpectation(UTUtils.defaultFactors),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b3").build()
        )
    );
    result.check();
  }

  @Test(expected = JCUnitAssertionError.class)
  public void sanityE02() throws Exception {
    VerificationResult result = verify(
        new SanityExpectation(UTUtils.defaultFactors),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").put("E", "e1").build()
        )
    );
    result.check();
    assertThat(result.isSuccessful(), is(true));
  }

  @Test
  public void constraintViolationN01() throws Exception {
    VerificationResult result = verify(
        new NoConstraintViolationExpectation(ConstraintManager.DEFAULT_CONSTRAINT_MANAGER),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        )
    );
    result.check();
  }

  @Test(expected = JCUnitAssertionError.class)
  public void constraintViolationE01() throws Exception {
    VerificationResult result = verify(
        new NoConstraintViolationExpectation(new ConstraintManagerBase() {
          @Override
          public boolean check(Tuple tuple) throws UndefinedSymbol {
            return !(new Tuple.Builder().put("A", "a1").put("B", "b1").build().equals(tuple));
          }
        }),
        UTUtils.tuples(
            UTUtils.tupleBuilder().put("A", "a1").put("B", "b1").build()
        )
    );
    result.check();
  }

  public VerificationResult verify(Class<? extends Expectation> klazz, Tuple[] expect, Tuple[] tuples) throws Exception {
    Checks.checknotnull(klazz);
    Checks.checknotnull(expect);
    Checks.checknotnull(tuples);

    Expectation e = klazz.getDeclaredConstructor(Collection.class).newInstance(asList(expect));
    VerificationResult result = e.verify(asList(tuples));
    return result;
  }

  public VerificationResult verify(Expectation expect, Tuple[] tuples) {
    return Checks.checknotnull(expect).verify(asList(Checks.checknotnull(tuples)));
  }
}
