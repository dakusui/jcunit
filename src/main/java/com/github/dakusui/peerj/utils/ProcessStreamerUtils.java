package com.github.dakusui.peerj.utils;

import com.github.dakusui.printables.PrintablePredicate;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum ProcessStreamerUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStreamerUtils.class);

  private static Checker.StreamChecker createStreamChecker(String... regexes) {
    List<Pattern> patterns = Arrays.stream(regexes)
        .map(Pattern::compile)
        .collect(toList());
    return new Checker.StreamChecker() {
      List<String> foundErrors = new LinkedList<>();

      @Override
      public boolean getAsBoolean() {
        return foundErrors.isEmpty();
      }

      @Override
      public void accept(String s) {
        for (Pattern each : patterns) {
          Matcher m = each.matcher(s);
          if (m.find())
            foundErrors.add(m.replaceAll("<" + each + ">"));
        }
      }

      @Override
      public String toString() {
        return "Following errors are found:" +
            foundErrors
                .stream()
                .collect(joining("[", format("%n- "), "]"));
      }
    };
  }

  public static Stream<String> streamFile(File file) {
    return processStreamer(format("cat %s", file.getAbsolutePath()),
        Checker.createDefault())
        .stream();
  }

  public static ProcessStreamer processStreamer(String command, Checker checker) {
    LOGGER.debug("Executing:[{}]", command);
    return new ProcessStreamer.Builder(Shell.local(), command)
        .checker(checker)
        .build();
  }

  public static class StandardChecker implements Checker {
    private final StreamChecker stdoutChecker;
    private final StreamChecker stderrChecker;

    public StandardChecker(String... regexes) {
      stdoutChecker = createStreamChecker(regexes);
      stderrChecker = createStreamChecker(regexes);

    }

    @Override
    public StreamChecker forStdOut() {
      return stdoutChecker;
    }

    @Override
    public StreamChecker forStdErr() {
      return stderrChecker;
    }

    @Override
    public Predicate<Integer> exitCodeChecker() {
      return new PrintablePredicate.Builder<Integer>(i -> Objects.equals(i, 0)).describe("==[0]");
    }
  }
}
