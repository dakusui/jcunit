package com.github.dakusui.peerj.testbases;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.IoUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.peerj.PeerJUtils2.*;
import static java.lang.String.format;

public abstract class PeerJScratch extends PeerJBase {

  public PeerJScratch(Spec spec) {
    super(spec);
  }

  @Before
  public void before() {
    Utils.invalidateMemos();
  }
}
