package com.github.dakusui.peerj.ext.shared;

import com.github.dakusui.peerj.PeerJUtils2;
import com.github.dakusui.peerj.utils.ProcessStreamerUtils;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public enum IoUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(IoUtils.class);

  public static File outFile(File baseDir) {
    return new File(baseDir, "acts.ca");
  }

  public static File inFile(File baseDir) {
    return new File(baseDir, "acts.xml");
  }

  public static void main(String... args) {
    System.getProperties().forEach((k, v) -> System.out.println(k + "=" + v));
    List<String> actual = asList("d", "a", "b", "c");
    assertThat(actual, asListOf(String.class).containsExactly(asList("b", "c", "d", "a", "e")).$());
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
}
