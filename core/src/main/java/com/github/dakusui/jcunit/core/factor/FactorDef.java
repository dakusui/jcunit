package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.regex.Composer;
import com.github.dakusui.jcunit.regex.Expr;
import com.github.dakusui.jcunit.regex.Parser;
import com.github.dakusui.jcunit.regex.RegexToFactorListTranslator;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

public abstract class FactorDef {
  /**
   * Name of a field to which a value synthesized by this object through {@code compose}
   * method should be assigned.
   */
  public final String name;

  public FactorDef(String name) {
    this.name = checknotnull(name);
  }

  /**
   * Adds a list of factors to given {@code Factors.Builder} object.
   * The builder object will then be used to build factors in {@code FactorSpace}
   * object.
   *
   * @param factorsBuilder A builder to which synthesized factors will be added.
   */
  public abstract void addTo(Factors.Builder factorsBuilder);

  public abstract List<Factor> getFactors();

  public abstract ConstraintChecker createConstraintChecker();

  public abstract void compose(Tuple.Builder b, Tuple in);

  public static class Simple extends FactorDef {
    private final LevelsProvider levelsProvider;

    public Simple(String name, LevelsProvider levelsProvider) {
      super(name);
      this.levelsProvider = checknotnull(levelsProvider);
    }

    @Override
    public void addTo(Factors.Builder factorsBuilder) {
      Factor.Builder b = new Factor.Builder(this.name);
      for (int i = 0; i < this.levelsProvider.size(); i++) {
        b.addLevel(this.levelsProvider.get(i));
      }
      factorsBuilder.add(b.build());
    }

    @Override
    public List<Factor> getFactors() {
      Factor.Builder b = new Factor.Builder(this.name);
      for (int i = 0; i < this.levelsProvider.size(); i++) {
        b.addLevel(this.levelsProvider.get(0));
      }
      return Collections.singletonList(b.build());
    }

    @Override
    public ConstraintChecker createConstraintChecker() {
      return ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER;
    }

    @Override
    public void compose(Tuple.Builder b, Tuple in) {
      b.put(this.name, in.get(this.name));
    }
  }


  public static class Fsm<T> extends FactorDef {

    private final FSM<T>                                  fsm;
    private final int                                     historyLength;
    private final List<Parameters.LocalConstraintChecker> localCMs;
    private final FSMFactors                              fsmFactors;


    public Fsm(String name, FSM<T> fsm, List<Parameters.LocalConstraintChecker> constraintCheckers, int historyLength) {
      super(name);
      this.fsm = checknotnull(fsm);
      checkcond(historyLength > 0);
      this.historyLength = historyLength;
      this.fsmFactors = new FSMFactors.Builder(this.name, this.fsm, this.historyLength).build();
      this.localCMs = Collections.unmodifiableList(Utils.newList(constraintCheckers));
    }

    @Override
    public void addTo(Factors.Builder factorsBuilder) {
      String fsmName = this.name;
      FSM<?> fsm = this.fsm;
      for (int index = 0; index < historyLength; index++) {
        ////
        // Build a factor for {index}th state
        {
          Factor.Builder bb = new Factor.Builder(stateName(fsmName, index));
          for (State each : fsm.states()) {
            bb.addLevel(each);
          }
          factorsBuilder.add(bb.build());
        }
        ////
        // Build a factor for {index}th action
        // {i}th element of allParams (List<Object>) is a list of possible levels
        //
        final List<Set<Object>> allParams = new ArrayList<Set<Object>>();
        ////
        // 'smallestNumParams' holds the smallest number of parameters of
        // 'action' methods.
        // All the actions share the same parameter factors.
        // This means some parameter factors (e.g., the last one) will not be
        // used sometimes unless corresponding action's level is set to the method
        // with the most parameters.
        int smallestNumParams = Integer.MAX_VALUE;
        {
          Factor.Builder bb = new Factor.Builder(actionName(fsmName, index));
          for (Action each : fsm.actions()) {
            bb.addLevel(each);
            if (each.numParameterFactors() < smallestNumParams)
              smallestNumParams = each.numParameterFactors();
            for (int i = 0; i < each.numParameterFactors(); i++) {
              if (i >= allParams.size()) {
                allParams.add(new LinkedHashSet<Object>());
              }
              Object[] paramValues = each.parameterFactorLevels(i);
              for (Object v : paramValues) {
                allParams.get(i).add(v);
              }
            }
          }
          factorsBuilder.add(bb.build());
        }
        ////
        // Build factors for {index}th action's parameters
        {
          int i = 0;
          for (Set<Object> each : allParams) {
            Factor.Builder bb = new Factor.Builder(paramName(fsmName, index, i++));
            if (i >= smallestNumParams)
              ////
              // Add VOID action as a level. 'smallestNumParams' is the number of
              // parameters of the method with the least parameters.
              // Parameter factors after this point must have VOID level.
              // Because if a method whose parameters are little than the largest,
              // it means the last some parameters cannot have any arguments.
              bb.addLevel(FSMFactors.VOID);
            for (Object v : each) {
              bb.addLevel(v);
            }
            factorsBuilder.add(bb.build());
          }
        }
      }
    }

    @Override
    public List<Factor> getFactors() {
      return this.fsmFactors.asFactorList();
    }

    @Override
    public ConstraintChecker createConstraintChecker() {
      return new FSMConstraintChecker(
          this.name,
          this.historyLength,
          this.fsmFactors,
          this.localCMs);
    }

    @Override
    public void compose(Tuple.Builder b, Tuple in) {
      ScenarioSequence<?> mainSequence = new ScenarioSequence.BuilderFromTuple<Object>()
          .setFSMName(this.name)
          .setHistoryLength(this.historyLength)
          .setTuple(in)
          .build();
      //noinspection unchecked
      b.put(this.name, new Story(
          this.name,
          new StateRouter.Base(this.fsm).routeTo(mainSequence.state(0)),
          mainSequence
      ));
    }


    public static String stateName(String fsmName, int i) {
      return String.format("FSM:%s:state:%d", fsmName, i);
    }

    public static String actionName(String fsmName, int i) {
      return String.format("FSM:%s:action:%d", fsmName, i);
    }

    public static String paramName(String fsmName, int i, int j) {
      return String.format("FSM:%s:param:%d:%d", fsmName, i, j);
    }
  }

  public static class Regex extends FactorDef {
    private final Factors                   factors;
    private final List<TestSuite.Predicate> constraints;
    private final Expr                      expr;

    public Regex(String name, String sequence) {
      super(name);
      RegexToFactorListTranslator builder = new RegexToFactorListTranslator(this.name);
      this.expr = new Parser().parse(sequence);
      this.expr.accept(builder);
      this.factors = builder.buildFactors();
      this.constraints = builder.buildConstraints(this.factors.asFactorList());
    }

    @Override
    public void addTo(Factors.Builder factorsBuilder) {
      for (Factor each : this.factors) {
        factorsBuilder.add(each);
      }
    }

    @Override
    public List<Factor> getFactors() {
      return this.factors.asFactorList();
    }

    @Override
    public ConstraintChecker createConstraintChecker() {
      return new TestSuite.StandardConstraintChecker(this.factors.asFactorList(), this.constraints);
    }

    @Override
    public void compose(Tuple.Builder b, Tuple in) {
      b.put(this.name, new Composer(this.name, expr).compose(in));
    }
  }
}
