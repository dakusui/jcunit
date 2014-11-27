package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ScenarioSequence<SUT> {
    int size();

    Scenario<SUT> get(int i);

    State<SUT> state(int i);

    Action<SUT> action(int i);

    Args args(int i);

    /**
     * Builds a {@code ScenarioSequence} object from a {@code Tuple} and a {@code FactorNameResolver}.
     *
     * @param <SUT> A class of software under test.
     */
    public static class Builder<SUT> {
        private FactorNameResolver resolver;
        private Tuple tuple;

        public Builder() {
        }

        public Builder setFactorNameResolver(FactorNameResolver resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder setTuple(Tuple tuple) {
            this.tuple = tuple;
            return this;
        }

        public ScenarioSequence<SUT> build() {
            Checks.checknotnull(tuple);
            Checks.checknotnull(resolver);
            Checks.checkcond(resolver.historyLength() > 0);
            return new ScenarioSequence<SUT>() {
                @Override
                public Scenario<SUT> get(int i) {
                    Checks.checkcond(i >= 0);
                    Checks.checkcond(i < this.size());
                    State<SUT> given = this.state(i);
                    Action<SUT> when = this.action(i);
                    Args with = this.args(i);
                    return new Scenario<SUT>(given, when, with);
                }

                @Override
                public State<SUT> state(int i) {
                    Checks.checkcond(i >= 0);
                    Checks.checkcond(i < this.size());
                    return (State<SUT>) tuple.get(resolver.stateFactorName(i));
                }

                @Override
                public Action<SUT> action(int i) {
                    Checks.checkcond(i >= 0);
                    Checks.checkcond(i < this.size());
                    return (Action<SUT>) tuple.get(resolver.actionFactorName(i));
                }

                @Override
                public Args args(int i) {
                    Checks.checkcond(i >= 0);
                    Checks.checkcond(i < this.size());
                    Object[] values = new Object[action(i).numParams()];
                    for (int j = 0; j < values.length; j++) {
                        values[j] = tuple.get(resolver.paramFactorName(i, j));
                    }
                    return new Args(values);
                }

                @Override
                public int size() {
                    return resolver.historyLength();
                }
            };
        }
    }
}
