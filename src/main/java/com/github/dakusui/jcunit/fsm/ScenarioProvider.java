package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class ScenarioProvider<SUT> extends LevelsProviderBase<ScenarioSequence<SUT>> {
    private List<ScenarioSequence> scenarioSequences;

    @Override
    protected void init(Field targetField, FactorField annotation,
                        Object[] parameters) {
        TupleGenerator generator = createTupleGenerator(targetField, annotation,
                parameters, 3);
        List<ScenarioSequence<SUT>> partialScenarioSequences = new ArrayList<ScenarioSequence<SUT>>();
        for (long i = 0; i < generator.size(); i++) {
            Tuple tuple = generator.get(i);
            partialScenarioSequences.add(
                    createScenarioSequenceFromTuple(tuple));
        }
        this.scenarioSequences = organizeScenarioSequences(partialScenarioSequences);
    }

    private List<ScenarioSequence> organizeScenarioSequences(
            List<ScenarioSequence<SUT>> partialScenarioSequences) {
        return null;
    }

    private ScenarioSequence<SUT> createScenarioSequenceFromTuple(
            Tuple tuple) {
        return null;
    }

    @Override
    public int size() {
        return this.scenarioSequences.size();
    }

    @Override
    public ScenarioSequence<SUT> get(int n) {
        return this.scenarioSequences.get(n);
    }

    private TupleGenerator createTupleGenerator(Field targetField,
                                                FactorField annotation,
                                                Object[] parameters, int historySize) {
        Class<? extends TupleGenerator> tupleGeneratorClass = IPO2TupleGenerator.class;
        ConstraintManager constraintManager = createConstraintManager();
        TupleGenerator ret = new TupleGenerator.Builder()
                .setTupleGeneratorClass(tupleGeneratorClass)
//                .setFactors(loadFactors(createFSM(), historySize()))
                .setConstraintManager(constraintManager)
                .setParameters(parameters)
                .build();
        return ret;
    }

    private void findRoutes(List<ScenarioSequence<SUT>> routes, FSM<SUT> fsm, State<SUT> from, State<SUT> to) {
        for (State each : fsm.states()) {

        }
    }

    protected ConstraintManager createConstraintManager() {
        return new ConstraintManagerBase() {
            @Override
            public boolean check(Tuple tuple) throws UndefinedSymbol {
                return false;
            }
        };
    }

    protected abstract FSM createFSM();

    protected abstract int historySize();

}
