package com.github.dakusui.jcunit.compat.generators;

import com.github.dakusui.jcunit.constraints.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IPO2TestArrayGenerator<T> extends BaseTestArrayGenerator<T> {
  List<Tuple> tests;
  Map<T, Factor> keyToFactorMap = new HashMap<T, Factor>();

  @Override
  public long initializeTestCases(GeneratorParameters.Value[] params,
      LinkedHashMap<T, Object[]> domains) {
    super.init(params, domains);
    Factors.Builder factorsBuilder = new Factors.Builder();
    for (T k : domains.keySet()) {
      String name = String.format("F-%s-%d", k, System.identityHashCode(k));
      Factor.Builder factorBuilder = new Factor.Builder();
      ////
      // Add all the levels to the factor builder object in the same order as
      // in 'domains' map.
      factorBuilder.setName(name);
      for (Object v : domains.get(k)) {
        factorBuilder.addLevel(v);
      }
      if (factorBuilder.getLevels().size() > 0) {
        Factor factor = factorBuilder.build();
        this.keyToFactorMap.put(k, factor);
        factorsBuilder.add(factor);
      }
    }
    Factors factors = factorsBuilder.build();
    IPO2 ipo2 = new IPO2(factors, 2, new NullConstraintManager(),
        new GreedyIPO2Optimizer());
    ipo2.ipo();
    this.tests = ipo2.getResult();
    return this.tests.size();
  }

  @Override
  public int getIndex(T key, long cur) {
    Utils.checkcond(keyToFactorMap.containsKey(key),
        String.format("This key '%s' mustn't be referenced.", key));
    Tuple test = this.tests.get((int) cur);
    Factor f = keyToFactorMap.get(key);
    ////
    // Relying on the fact that levels in the factor and ones in domains map
    // are placed in the same order.
    return f.levels.indexOf(test.get(f.name));
  }
}
