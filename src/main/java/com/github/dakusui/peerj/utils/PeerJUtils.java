package com.github.dakusui.peerj.utils;

import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.FactorSpaceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public enum PeerJUtils {
  ;
  private static final Logger LOGGER = LoggerFactory.getLogger(PeerJUtils.class);

  public static File createTempDirectory(String pathname) {
    try {
      File dir = new File(pathname);
      LOGGER.debug("{} was created={}", dir, dir.mkdirs());
      return Files.createTempDirectory(dir.toPath(), "jcunit-").toFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static FactorSpaceSpec createFactorySpaceSpec(ConstraintSet constraintSet, final String prefix, int degree, int baseStrength, int relationStrength) {
    return new FactorSpaceSpec(prefix) {{
      FactorSpaceSpec factorSpaceSpec = this.constraintSetName(constraintSet.name());
      for (int offset = 0; offset < degree; offset += 10)
        constraintSet.constraintFactory(offset).ifPresent(factorSpaceSpec::addConstraint);
    }}.baseStrength(baseStrength)
        .relationStrength(relationStrength);
  }
}
