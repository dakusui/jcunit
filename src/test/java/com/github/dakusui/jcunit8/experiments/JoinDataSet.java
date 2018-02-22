package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface JoinDataSet {
  static SchemafulTupleSet load(int doi, int numFactors) {
    return load(doi, numFactors, i -> i < 10 ?
        String.format("l%03d", i) :
        String.format("p%03d", i - 10));
  }

  static SchemafulTupleSet load(int doi, int numFactors, IntFunction<String> formatter) {
    assert doi == 2 || doi == 3;
    assert numFactors > 0;
    assert numFactors % 10 == 0;
    class Util {
      private List<List<Integer>> readData(int doi, int numFactors) {
        assert doi == 2 || doi == 3;
        assert numFactors > 0;
        assert numFactors % 10 == 0;
        assert numFactors <= 110;
        String code = numFactors == 110 ?
            "a" :
            Integer.toString(numFactors / 10 - 1);
        URL url = ClassLoader.getSystemClassLoader().getResource(
            String.format(
                "%s/doi%s-L4-10xR4-10#%s.csv",
                JoinDataSet.class.getCanonicalName(),
                doi,
                code
            )
        );
        assert url != null;
        BufferedReader br = null;
        try {
          br = new BufferedReader(new InputStreamReader(url.openStream()));
          String line;
          List<List<Integer>> ret = new LinkedList<>();
          while ((line = br.readLine()) != null) {
            if (!line.matches("[0-9,]+$"))
              continue;
            ret.add(Arrays.stream(line.split(",")).map(Integer::parseInt).collect(Collectors.toList()));
          }
          return ret;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      }
    }
    Util util = new Util();
    return new SchemafulTupleSet.Builder(
        IntStream.range(0, numFactors).mapToObj(
            formatter::apply
        ).collect(Collectors.toList())
    ) {{
      util.readData(doi, numFactors).stream().map(
          (List<Integer> d) -> {
            Tuple.Builder builder = Tuple.builder();
            for (int i = 0; i < d.size(); i++)
              builder.put(formatter.apply(i), d.get(i));
            return builder.build();
          }
      ).forEach(
          this::addEntry
      );
    }}.build();
  }
}