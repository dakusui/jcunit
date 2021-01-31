package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.peerj.PeerJUtils2;
import com.github.dakusui.peerj.utils.ProcessStreamerUtils;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public enum IoUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);

  public static File outFile(File baseDir) {
    return new File(baseDir, "acts.ca");
  }

  public static File inFile(File baseDir) {
    return new File(baseDir, "acts.xml");
  }

  public static void writeTo(File file, Stream<String> stream) {
    try {
      try (OutputStreamWriter writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
        stream.peek(LOGGER::trace).forEach(line -> PeerJUtils2.write(writer, String.format("%s%n", line)));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeTo(File file, String data) {
    ProcessStreamerUtils.processStreamer(format("echo '%s' > %s", data, file.getAbsolutePath()), ProcessStreamer.Checker.createDefault())
        .stream()
        .forEach(LOGGER::debug);
  }

  public static List<Tuple> readTestSuiteFromXsv(Stream<String> data, String separatorRegex) {
    AtomicReference<List<String>> header = new AtomicReference<>();
    return data.filter(s -> !s.startsWith("#"))
        .filter(s -> {
          if (header.get() == null) {
            header.set(asList(s.split(separatorRegex)));
            return false;
          }
          return true;
        })
        .map(
            s -> {
              List<String> record = asList(s.split(separatorRegex));
              List<String> h = header.get();
              if (record.size() != h.size()) {
                LOGGER.debug("header:" + h);
                LOGGER.debug("record:" + record);
                throw new IllegalArgumentException("size(header)=" + h.size() + ", size(record)=" + record.size());
              }
              Tuple.Builder b = Tuple.builder();
              for (int i = 0; i < h.size(); i++)
                b.put(h.get(i), record.get(i));
              return b.build();
            }
        )
        .collect(toList());
  }

  public static String newLine() {
    return format("%n");
  }
}
