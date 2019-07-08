package com.github.dakusui.jcunit8.experiments.join.acts;

import com.github.dakusui.printables.PrintablePredicate;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
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

enum ProcessStreamerUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(ActsUtils.class);

  private static ProcessStreamer.Checker.StreamChecker createStreamChecker(String... regexes) {
    List<Pattern> patterns = Arrays.stream(regexes)
        .map(Pattern::compile)
        .collect(toList());
    return new ProcessStreamer.Checker.StreamChecker() {
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

  static Stream<String> streamFile(File file) {
    return processStreamer(format("cat %s", file.getAbsolutePath()), ProcessStreamer.Checker.createDefault()).stream();
  }

  static ProcessStreamer processStreamer(String command, ProcessStreamer.Checker checker) {
    LOGGER.debug("Executing:[{}]", command);
    return new ProcessStreamer.Builder(Shell.local(), command)
        .checker(checker)
        .build();
  }

  static void writeTo(File file, String data) {
    processStreamer(format("echo '%s' > %s", data, file.getAbsolutePath()), ProcessStreamer.Checker.createDefault())
        .stream()
        .forEach(LOGGER::debug);
  }

  static class StandardChecker implements ProcessStreamer.Checker {
    private final StreamChecker stdoutChecker;
    private final StreamChecker stderrChecker;

    StandardChecker(String... regexes) {
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
