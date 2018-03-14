package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class LucasTest {
  @Test
  public void given3_3_doi2$whenChooseFactorNames$thenWorksFine() {
    assertThat(
        new Lucas.Session().streamFactorNameLists(
            asList("l0", "l1", "l2"),
            asList("r0", "r1", "r2"),
            asList("c0", "c1", "c2"),
            2
        ).collect(toList()),
        allOf(
            asInteger("size").eq(9).$(),
            asObject("get", 0).equalTo(asList("l0", "c0")).$(),
            asObject("get", 1).equalTo(asList("l0", "c1")).$(),
            asObject("get", 2).equalTo(asList("l0", "c2")).$(),
            asObject("get", 3).equalTo(asList("l1", "c0")).$(),
            asObject("get", 4).equalTo(asList("l1", "c1")).$(),
            asObject("get", 5).equalTo(asList("l1", "c2")).$(),
            asObject("get", 6).equalTo(asList("l2", "c0")).$(),
            asObject("get", 7).equalTo(asList("l2", "c1")).$(),
            asObject("get", 8).equalTo(asList("l2", "c2")).$()
        )
    );
  }

  @Test
  public void given2_3_doi2$whenChooseFactorNames$thenWorksFine() {
    assertThat(
        new Lucas.Session().streamFactorNameLists(
            asList("l0", "l1"),
            asList("r0", "r1"),
            asList("c0", "c1"),
            3
        ).collect(toList()),
        allOf(
            asInteger("size").eq(12).$(),
            asObject("get", 0).equalTo(asList("l0", "r0", "c0")).$(),
            asObject("get", 1).equalTo(asList("l0", "r0", "c1")).$(),
            asObject("get", 2).equalTo(asList("l0", "r1", "c0")).$(),
            asObject("get", 3).equalTo(asList("l0", "r1", "c1")).$(),
            asObject("get", 4).equalTo(asList("l1", "r0", "c0")).$(),
            asObject("get", 5).equalTo(asList("l1", "r0", "c1")).$(),
            asObject("get", 6).equalTo(asList("l1", "r1", "c0")).$(),
            asObject("get", 7).equalTo(asList("l1", "r1", "c1")).$(),
            asObject("get", 8).equalTo(asList("l0", "c0", "c1")).$(),
            asObject("get", 9).equalTo(asList("l1", "c0", "c1")).$(),
            asObject("get", 10).equalTo(asList("l0", "l1", "c0")).$(),
            asObject("get", 11).equalTo(asList("l0", "l1", "c1")).$()
        )
    );
  }
}
