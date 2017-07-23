package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterSpec;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(FlyingSpaghttiMonsterExampleWithSeeds.Config.class)
public class FlyingSpaghttiMonsterExampleWithSeeds extends FlyingSpaghettiMonsterExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
          Tuple.builder().putFsm(
              Scenario.builder(
                  "flyingSpaghettiMonster",
                  FlyingSpaghettiMonsterSpec.class
              ).startFrom(
                  FlyingSpaghettiMonsterSpec.FLYING
              ).doAction(
                  "cook",
                  "meat sauce"
              ).build()
          ).build()
      ).build();
    }
  }

}
