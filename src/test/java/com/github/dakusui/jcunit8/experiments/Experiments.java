package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.experiments.compat.JoinExperiments;
import com.github.dakusui.jcunit8.experiments.compat.StandardFactorSpaces;
import com.github.dakusui.jcunit8.experiments.generation.GenerationExperiment;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    StandardFactorSpaces.class,
    GenerationExperiment.class,
    JoinExperiments.class
})
public class Experiments {
}
