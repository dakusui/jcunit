package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.plugins.caengines.RandomCoveringArrayEngine;
import com.github.dakusui.jcunit.regex.RegexLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = RandomCoveringArrayEngine.class, args = { @Value("50") }))
public class MessageDigestExample {

  private MessageDigest messageDigest;

  @FactorField
  public int i;

  @FactorField(
      levelsProvider = RegexLevelsProvider.class,
      args = { @Value({ "reset{0,1}(update1|update2){1,2}(digest0|digest1)" }) }
  )
  public List<String> scenario;

  @Before
  public void before() throws NoSuchAlgorithmException {
    this.messageDigest = MessageDigest.getInstance("MD5");
  }

  @Test
  public void test() {
    System.out.println(scenario);
  }
}
