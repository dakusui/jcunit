package com.github.dakusui.jcunit8.pipeline.stages.joiners.old;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.text.DateFormat;
import java.util.*;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SimilarityBasedJoiner extends Joiner.Base {
  private final Requirement requirement;

  public SimilarityBasedJoiner(Requirement requirement) {
    this.requirement = requireNonNull(requirement);
  }

  /*
   |lhs| >= |rhs|
   */
  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    Session session = new Session(lhs, rhs);
    Set<Tuple> work = new LinkedHashSet<>();
    int d = this.requirement.strength();

    ////
    // Prepare rhs sub-tuple set of specified strength
    TupleSet subtuplesFromRhs = rhs.subtuplesOf(d - 1);

    ////
    // hg
    List<SchemafulTupleSet> joined = lhs.getAttributeNames().stream()
        .map(k -> hg(lhs.project(singletonList(k)), rhs, subtuplesFromRhs))
        .collect(toList());

    ////
    // bunch
    OryzaBunch oryzaBunch = session.bunchTuples(joined, lhs, rhs);

    ////
    // thrash
    lhs.forEach(
        eachFromLhs -> oryzaBunch.find(eachFromLhs)
    );

    ////
    // vg
    return new SchemafulTupleSet.Builder(
        Stream.concat(
            lhs.getAttributeNames().stream(),
            rhs.getAttributeNames().stream()
        ).collect(
            toList()
        )
    ).addAllEntries(work).build();
  }

  protected long sizeOf(SchemafulTupleSet tupleSet) {
    return tupleSet.size() * tupleSet.width();
  }

  private SchemafulTupleSet hg(SchemafulTupleSet eachFromLhs, SchemafulTupleSet rhs, TupleSet subtuplesFromRhs) {
    return null;
  }


  static class OryzaBunch {
    final LinkedHashSet<Oryza> oryzae;

    OryzaBunch(LinkedHashSet<Oryza> oryzae) {
      assert oryzae.stream().map(o -> o.straw).count() == oryzae.size();
      this.oryzae = checknotnull(oryzae);
    }

    Optional<Oryza> find(Tuple straw) {
      return oryzae.stream()
          .filter(oryza -> Objects.equals(oryza.straw, straw))
          .findFirst();
    }

    static class Builder {
      Map<Tuple, Oryza.Builder> work = new LinkedHashMap<>();

      Builder add(Tuple eachFromLhs, Tuple rhs) {
        checkcond(checknotnull(eachFromLhs).size() == 1);
        if (!work.containsKey(rhs))
          work.put(rhs, new Oryza.Builder(rhs));
        work.get(rhs).add(eachFromLhs);
        return this;
      }

      OryzaBunch build() {
        return new OryzaBunch(new LinkedHashSet<>(
            work.keySet().stream()
                .map(tuple -> work.get(tuple).build())
                .collect(toList())
        ));
      }
    }
  }

  static class Oryza {
    final Tuple                    straw;
    final Map<String, Set<Object>> spike;

    private Oryza(Tuple straw, Map<String, Set<Object>> spike) {
      this.straw = straw;
      this.spike = spike;
    }

    int countMatchesInSpike(Tuple tuple) {
      return (int) this.spike.keySet().stream()
          .filter(k -> Objects.equals(spike.get(k), tuple.get(k)))
          .count();
    }

    static class Builder {
      private final Map<String, Set<Object>> spike;
      private final Tuple                    straw;

      Builder(Tuple straw) {
        this.straw = checknotnull(straw);
        this.spike = new LinkedHashMap<>();
      }

      Builder add(Tuple tuple) {
        checknotnull(tuple).keySet().stream()
            .peek(k -> {
              if (!spike.containsKey(k))
                spike.put(k, new LinkedHashSet<>());
            })
            .forEach(
                k -> spike.get(k).add(tuple.get(k))
            );
        return this;
      }

      Oryza build() {
        return new Oryza(straw, spike);
      }
    }
  }

  static class Session {
    final SchemafulTupleSet lhs;
    final SchemafulTupleSet rhs;

    Session(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    private OryzaBunch bunchTuples(List<SchemafulTupleSet> joined, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      OryzaBunch.Builder b = new OryzaBunch.Builder();
      joined.stream()
          .flatMap(Collection::stream)
          .forEach(tuple ->
              b.add(this.project(tuple, lhs.getAttributeNames()), this.project(tuple, rhs.getAttributeNames()))
          );
      return b.build();
    }

    public Tuple project(Tuple tuple, List<String> attributeNames) {
      return Utils.project(attributeNames, tuple);
    }
  }

  enum Helper {
    ;

    static double similarity(Tuple t, Tuple u) {
      Tuple v;
      if (t.size() < u.size()) {
        v = t;
        t = u;
        u = v;
      }
      double ret = 0;
      for (String k : u.keySet())
        ret += Objects.equals(t.get(k), u.get(k)) ?
            1 :
            0;
      ret = ret / (double) u.keySet().size();
      return ret;
    }
  }

  public static void main(String... args)  {
    System.out.println(new Locale("ja", "jp", "jp"));
//    System.out.println(new Date());
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, new Locale("ja", "jp", "jp"));
    System.out.println(dateFormat.format(new Date()));
  }
}
