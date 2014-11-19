package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.LinkedHashMap;

/**
 */
public class ScenarioFactorsBuilder<SUT> {
    private int length = 1;
    private FSM<SUT> fsm;

    public ScenarioFactorsBuilder<SUT> setFSM(FSM<SUT> fsm) {
        Checks.checknotnull(fsm);
        this.fsm = fsm;
        return this;
    }

    public ScenarioFactorsBuilder<SUT> setLength(int length) {
        Checks.checkcond(length > 0);
        this.length = length;
        return this;
    }

    public Factors build() {
        Factors.Builder b = new Factors.Builder();
        LinkedHashMap<String, Factor> allParams = new LinkedHashMap<String, Factor>();
        for (int i = 0; i < this.length; i++) {
            {
                Factor.Builder bb = new Factor.Builder();
                bb.setName(stateName(i));
                for (State each : fsm.states()) {
                    bb.addLevel(each);
                }
                b.add(bb.build());
            }
            {
                Factor.Builder bb = new Factor.Builder();
                bb.setName(actionName(i));
                for (Action each : fsm.actions()) {
                    bb.addLevel(each);
                    for (Factor eachParam : each.params()) {
                        if (!allParams.containsKey(eachParam.name))
                            allParams.put(eachParam.name, eachParam);
                    }
                }
                b.add(bb.build());
            }
        }
        for (Factor each : allParams.values()) {
            b.add(each);
        }
        return b.build();

    }

    public String stateName(int i) {
        return String.format("FSM:state:%d", i);
    }

    public String actionName(int i) {
        return String.format("FSM:action:%d", i);
    }

    public String paramName(String actionName, int i) {
        return String.format("%s:param:%d", actionName, i);
    }

}
