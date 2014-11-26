package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import org.hamcrest.Matcher;

public class Expectation<SUT> {
    public final State<SUT> state;
    public final Matcher returnedValue;
    public final Matcher thrownException;

    Expectation(State<SUT> state,
                Matcher returnedValue,
                Matcher thrownException) {
        this.state = state;
        this.returnedValue = returnedValue;
        this.thrownException = thrownException;
    }

    public Expectation(Matcher thrownException) {
        this(null, null, Checks.checknotnull(thrownException));
    }

    public Expectation(State<SUT> state,
                       Matcher returnedValue) {
        this(Checks.checknotnull(state), Checks.checknotnull(returnedValue), null);
    }
}
