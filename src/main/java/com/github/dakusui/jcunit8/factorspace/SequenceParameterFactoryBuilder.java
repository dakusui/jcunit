package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.pipeline.stages.Generator.VOID;
import static java.util.stream.Collectors.toList;

public class SequenceParameterFactoryBuilder<T> {
  private final List<?> elements;
  private       int     min;
  private       int     max;
  private boolean withRepetition = true;

  public SequenceParameterFactoryBuilder(List<? extends T> elements) {
    this.elements = elements;
  }

  public SequenceParameterFactoryBuilder<T> size(int min, int max) {
    Checks.checkcond(min >= 0);
    Checks.checkcond(max > 0);
    Checks.checkcond(max >= min);
    this.min = min;
    this.max = max;
    return this;
  }

  public SequenceParameterFactoryBuilder<T> size(int max) {
    return this.size(0, max);
  }

  public SequenceParameterFactoryBuilder<T> withRepetition() {
    return this.withRepetition(true);
  }

  public SequenceParameterFactoryBuilder<T> withRepetition(boolean enabled) {
    this.withRepetition = enabled;
    return this;
  }

  public SequenceParameterFactoryBuilder<T> withoutRepetition() {
    return this.withRepetition(false);
  }

  public Parameter.Factory<List<T>> build() {
    return new Parameter.Factory.Base<List<T>>() {
      @Override
      public Parameter<List<T>> create(String name) {
        return new Parameter.Base<List<T>>(name, knownValues) {
          @Override
          protected List<Factor> decompose() {
            return new LinkedList<Factor>() {
              {
                addAll(
                    IntStream.range(0, max).boxed(
                    ).map(
                        i -> Factor.create(
                            composeValueFactorName(i),
                            i < min ?
                                elements.toArray() :
                                Stream.concat(elements.stream(), Stream.of(VOID)).toArray()
                        )
                    ).collect(
                        toList()
                    )
                );
                add(
                    Factor.create(
                        composeSizeFactorName(),
                        IntStream.range(min, max + 1).boxed().toArray()
                    )
                );
              }
            };
          }

          @Override
          protected List<Constraint> generateConstraints() {
            return new LinkedList<Constraint>() {{
              add(new Constraint() {
                @Override
                public String getName() {
                  return String.format("excludeIllegalVoids:%s", involvedKeys());
                }

                @Override
                public boolean test(Tuple tuple) {
                  int size = getSizeFromTuple(tuple);
                  for (int i = min; i < max; i++) {
                    if (i < size) {
                      if (Objects.equals(getValueFromTuple(tuple, i), VOID))
                        return false;
                    } else if (!Objects.equals(tuple.get(composeValueFactorName(i)), VOID))
                      return false;
                  }
                  return true;
                }

                @Override
                public List<String> involvedKeys() {
                  return composeInvolvedKeys(min, max);
                }
              });
              if (!withRepetition) {
                add(new Constraint() {
                  @Override
                  public String getName() {
                    return String.format("forRepetition:%s", involvedKeys());
                  }

                  @Override
                  public boolean test(Tuple tuple) {
                    List<Object> work = composeListFrom(tuple, getSizeFromTuple(tuple)).stream(
                    ).filter(
                        v -> !Objects.equals(VOID, v)
                    ).collect(toList());
                    return work.size() == Utils.unique(work).size();
                  }

                  @Override
                  public List<String> involvedKeys() {
                    return composeInvolvedKeys(0, max);
                  }
                });
              }
            }};
          }

          @SuppressWarnings("unchecked")
          @Override
          public List<T> composeValue(Tuple tuple) {
            return (List<T>) composeListFrom(tuple, getSizeFromTuple(tuple));
          }

          @Override
          public Optional<Tuple> decomposeValue(List<T> value) {
            Checks.checktest(value.size() <= max, "");
            Checks.checktest(value.size() >= min, "");
            Tuple.Builder builder = Tuple.builder();
            for (int i = 0; i < max; i++) {
              if (i < value.size())
                builder.put(composeValueFactorName(i), value.get(i));
              else
                builder.put(composeValueFactorName(i), VOID);
            }
            builder.put(composeSizeFactorName(), value.size());
            return Optional.of(
                builder.build()
            );
          }

          private List<String> composeInvolvedKeys(int fromInclusive, int toExclusive) {
            return Stream.concat(IntStream.range(fromInclusive, toExclusive).mapToObj(
                this::composeValueFactorName
                ),
                Stream.of(composeSizeFactorName())
            ).collect(
                toList()
            );
          }

          private List<Object> composeListFrom(Tuple tuple, int toExclusive) {
            return IntStream.range(0, toExclusive).mapToObj(
                i -> getValueFromTuple(tuple, i)
            ).collect(
                toList()
            );
          }

          @SuppressWarnings("unchecked")
          private <U> U getValueFromTuple(Tuple tuple, int i) {
            return (U) tuple.get(composeValueFactorName(i));
          }

          private int getSizeFromTuple(Tuple tuple) {
            return (int) tuple.get(composeSizeFactorName());
          }

          private String composeValueFactorName(int i) {
            return String.format("%s-%02d", name, i);
          }

          private String composeSizeFactorName() {
            return String.format("%s-size", name);
          }
        };
      }
    };
  }
}
