package com.github.dakusui.jcunit8.experiments;

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
