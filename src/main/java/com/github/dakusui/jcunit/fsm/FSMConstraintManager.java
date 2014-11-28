package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.LinkedList;

/**
 */
public class FSMConstraintManager<SUT> extends ConstraintManagerBase {
    public static class Verifier<SUT> {

        public static class ErrorMessages extends LinkedList<String> {
        }

        boolean hasState(int i) {
            return false;
        }

        boolean hasAction(int i) {
            return false;
        }

        boolean verify(Action<SUT> action, Args args, ErrorMessages errors) {
            int numErrors = errors.size();

            int numArgs = args.size();
            int numParams = action.numParams();
            if (numArgs != numParams) errors.add("TODO");
            Object[] params = args.values();
            for (int i = 0; i < numArgs; i++) {
                if (i >= numParams) break;
                if (!isPossible(action.param(i), params[i]))
                    errors.add("TODO");
            }
            return numErrors == errors.size();
        }

        boolean verify(State<SUT> state, Action<SUT> action, Args args, ErrorMessages errors) {
            Checks.checknotnull(state);
            Checks.checknotnull(action);
            Checks.checknotnull(args);
            Checks.checknotnull(errors);

            return state.expectation(action, args) != null;
        }

        boolean verify(Scenario<SUT> preceding, State<SUT> state, ErrorMessages errors) {
            Checks.checknotnull(preceding);
            Checks.checknotnull(state);
            int numErrors = errors.size();

            return numErrors == errors.size() && preceding.then().state == state;
        }

        private boolean isPossible(Object[] possibleValues, Object value) {
            for (Object pv : possibleValues) {
                if (Utils.eq(pv, value)) return true;
            }
            return false;
        }
    }

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
        FSMFactors fsmFactors = (FSMFactors) this.getFactors();
        ScenarioSequence<SUT> seq = new ScenarioSequence.Builder<SUT>().setFactorNameResolver(fsmFactors).setTuple(tuple).build();
        for (int i = 0; i < fsmFactors.historyLength(); i++) {
            State<SUT> state = seq.state(i);
            Action<SUT> action = seq.action(i);
            if (state == null) throw new UndefinedSymbol();
            if (action == null) throw new UndefinedSymbol();

        }
        return false;
    }
}
