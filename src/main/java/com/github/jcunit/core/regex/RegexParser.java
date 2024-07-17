package com.github.jcunit.core.regex;

import com.github.jcunit.utils.InternalUtils;
import com.github.jcunit.exceptions.InvalidTestException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.jcunit.exceptions.Checks.checkcond;
import static com.github.jcunit.exceptions.Checks.checknotnull;
import static com.github.jcunit.core.regex.RegexParser.Type.ALT;
import static com.github.jcunit.core.regex.RegexParser.Type.CAT;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * A class to parse a "regular expression" of **JCUnitX**.
 *
 * Note that the regular expression is designed for **JCUnitX**, not a general purpose one.
 *
 */
public class RegexParser {
  /**
   * A pattern to match a quantifier in a regular expression, which looks like: `{0,1}`, `{1,23}`, and as such.
   */
  private static final Pattern QUANTIFIER_PATTERN = Pattern.compile("^\\{([0-9]+),([0-9]+)}");
  /*
   * We can implement the same mechanism by considering a white space a 'concatenation operator',
   * but it increases a number of factors and constraints generated.
   * This will result in poorer performance.
   * Instead, treat white spaces within a word just as part of the word, and after reverse
   * regex generation finishes, JCUnit will split into pieces.
   *
   * See RegexComposer, too.
   *
   * Limitation:: This pattern allows the second and the following leaves to start with numerics, which will not be valid as names in Java.
   */
  private static final Pattern LEAVES_PATTERN = Pattern.compile("^[A-Za-z_]([A-Za-z_0-9 ]*[A-Za-z_0-9])?");
  private final Expr.Factory exprFactory;

  public RegexParser() {
    this.exprFactory = new Expr.Factory();
  }

  public Expr parse(String regex) {
    return parse(preprocess(regex));
  }

  /**
   * Preprocesses an input string.
   *
   * @param input A string to be preprocessed.
   * @return A list of tokens after preprocessing
   */
  public static List<String> preprocess(String input) {
    List<String> ret = new LinkedList<>();
    List<String> read = new LinkedList<>();
    preprocess(read, ret, tokenizer(input), true);
    return ret;
  }

  enum Type {
    /**
     * Concatenation
     */
    CAT("*"),
    /**
     * Alternative
     */
    ALT("+"),;

    final String value;

    Type(String value) {
      this.value = value;
    }

    public String asString() {
      return "(" + this.value;
    }

    public String toString() {
      return asString();
    }
  }

  private enum SymbolType {
    OPEN,
    WORD,
    CHOICE,
    CLOSE;

    static SymbolType determine(String cur) {
      checknotnull(cur);
      if ("(".equals(cur)) {
        return OPEN;
      } else if ("|".equals(cur)) {
        return CHOICE;
      } else if (")".equals(cur)) {
        return CLOSE;
      }
      return WORD;
    }
  }

  private enum PreprocessingState {
    I, I_R, I_T,
    ALT_I, ALT_R, ALT_T,
    CAT_R, CAT_T,
    T
  }

  /**
   *
   * @param read
   * @param output The output list to which the current call writes its result.
   * @param input An iterator that holds the current input.
   * @param topLevel Tells this method if this call is the top level or not.
   */
  private static void preprocess(List<String> read, List<String> output, Iterator<String> input, boolean topLevel) {
    Type type = null;
    List<String> work = new LinkedList<>();
    try {
      PreprocessingState state = PreprocessingState.I;
      while (input.hasNext() && state != PreprocessingState.T) {
        String cur = input.next();
        read.add(cur);
        SymbolType symbolType = SymbolType.determine(cur);
        switch (state) {
        case I:
          switch (symbolType) {
          case OPEN:
            preprocess(read, work, input, false);
            state = PreprocessingState.I_T;
            break;
          case WORD:
            work.add(cur);
            state = PreprocessingState.I_T;
            break;
          default:
            throw syntaxError(cur, read);
          }
          break;
        case I_T:
          switch (symbolType) {
          case CHOICE:
            state = PreprocessingState.ALT_I;
            type = ALT;
            break;
          case WORD:
            state = PreprocessingState.CAT_T;
            type = CAT;
            work.add(cur);
            break;
          case OPEN:
            state = PreprocessingState.CAT_T;
            type = CAT;
            preprocess(read, work, input, false);
            break;
          case CLOSE:
            if (!topLevel) {
              state = PreprocessingState.T;
              break;
            }
          default:
            throw syntaxError(cur, read);
          }
          break;
        case ALT_I:
          switch (symbolType) {
          case OPEN:
            preprocess(read, work, input, false);
            state = PreprocessingState.ALT_T;
            break;
          case WORD:
            work.add(cur);
            state = PreprocessingState.ALT_T;
            break;
          default:
            throw syntaxError(cur, read);
          }
          break;
        case ALT_T:
          switch (symbolType) {
          case CHOICE:
            state = PreprocessingState.ALT_I;
            break;
          case CLOSE:
            state = PreprocessingState.T;
            break;
          default:
            throw syntaxError(cur, read);
          }
          break;
        case CAT_T:
          switch (symbolType) {
          case OPEN:
            state = PreprocessingState.CAT_T;
            preprocess(read, work, input, false);
            break;
          case WORD:
            work.add(cur);
            break;
          case CLOSE:
            state = PreprocessingState.T;
            break;
          default:
            throw syntaxError(cur, read);
          }
          break;
        case CAT_R:
          switch (symbolType) {
          case CLOSE:
            state = PreprocessingState.CAT_T;
            break;
          default:
            throw syntaxError(cur, read);
          }
          break;
        case T:
          throw syntaxError(cur, read);
        }
      }
      if (topLevel && input.hasNext()) {
        throw syntaxError(input.next(), work);
      }
      if (!asList(PreprocessingState.I_T, PreprocessingState.T, PreprocessingState.ALT_T, PreprocessingState.CAT_T).contains(state)) {
        throw inputShouldNotEndHere(state);
      }
    } finally {
      work.add(0, (type == null ? CAT : type).toString());
      work.add(")");
      output.addAll(work);
    }
  }

  private static RuntimeException syntaxError(String token, List<String> work) {
    throw new InvalidTestException(
        format(
            "token '%s' should not come after: '%s'",
            token,
            InternalUtils.joinBy("", work.subList(0, Math.max(0, work.size() - 1))))
    );
  }

  private static RuntimeException inputShouldNotEndHere(PreprocessingState state) {
    throw new InvalidTestException(format("Input should not end here: '%s'", state));
  }

  /**
   * Returns an iterator to read the input tokenizing it.
   *
   * @param input An input string.
   * @return An iterator that reads the tokenized input.
   */
  private static Iterator<String> tokenizer(final String input) {
    return new Iterator<String>() {
      String[] nextToken = nextToken(input);

      @Override
      public boolean hasNext() {
        return nextToken != null;
      }

      @Override
      public String next() {
        if (!hasNext())
          throw new NoSuchElementException();
        try {
          return nextToken[0];
        } finally {
          nextToken = nextToken(nextToken[1]);
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * The `input` cannot be `null`, otherwise the behavior of this function is not defined.
   * If the `input` is empty (a string whose length is 0), this function returns `null`.
   * If it starts with one of `(`, `)`, or `|`, it will return an array whose first element is the starting character
   * and the second is the rest of the string.
   * If it matches with `LEAVES_PATTERN`, an array whose first element is the longest possible matching part, and the second is the rest, will be returned.
   * If it matches with `QUANTIFIER_PATTERN` (`^\\{([0-9]+),([0-9]+)}`),
   *
   * @param input An input string to extract the next token.
   * @return
   */
  private static String[] nextToken(String input) {
    if (input.isEmpty())
      return null;
    char first = input.charAt(0);
    if (first == '(' || first == ')' || first == '|') {
      return new String[] { input.substring(0, 1), input.substring(1) };
    }
    {
      Matcher m = LEAVES_PATTERN.matcher(input);
      if (m.find()) {
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }
    {
      Matcher m = QUANTIFIER_PATTERN.matcher(input);
      if (m.find()) {
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }

    throw new InvalidTestException(format("Syntax error: Unparsable: '%s' did neither match '%s' nor '%s'", input, LEAVES_PATTERN, QUANTIFIER_PATTERN));
  }

  private Expr parse(List<String> tokens) {
    Context result = readTerm(tokens);
    checkcond(result.tokens.isEmpty(), "Syntax error: unparsed=%s", result.tokens);
    return result.expr;
  }

  private Context readTerm(List<String> tokens) {
    String head = head(tokens);
    Context ret;
    if (ALT.asString().equals(head)) {
      ret = readAlt(tail(tokens));
    } else if (CAT.asString().equals(head)) {
      ret = readCat(tail(tokens));
    } else {
      ret = readLeaf(tokens);
    }
    String nextHead;
    if (ret.tokens != null && (nextHead = head(ret.tokens)) != null) {
      Matcher m;
      if ((m = QUANTIFIER_PATTERN.matcher(nextHead)).find()) {
        ret = new Context(
            this.exprFactory.rep(ret.expr, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
            tail(ret.tokens));
      }
    }
    return ret;
  }

  private Context readLeaf(List<String> tokens) {
    String head = head(tokens);
    if (head == null)
      return Context.LAST;
    return new Context(this.exprFactory.leaf(head), tail(tokens));
  }

  private Context readAlt(List<String> tokens) {
    List<Expr> work = new LinkedList<Expr>();
    for (Context context = readTerm(tokens); context.hasNext(); context = readTerm(tokens)) {
      work.add(context.expr);
      tokens = context.tokens;
      if (")".equals(head(tokens))) {
        tokens = tail(tokens);
        break;
      }
    }
    if (work.size() == 1) {
      return new Context(work.get(0), tokens);
    }
    return new Context(this.exprFactory.alt(work), tokens);
  }

  private Context readCat(List<String> tokens) {
    List<Expr> work = new LinkedList<Expr>();
    for (Context context = readTerm(tokens); context.hasNext(); context = readTerm(tokens)) {
      work.add(context.expr);
      tokens = context.tokens;
      if (")".equals(head(tokens))) {
        tokens = tail(tokens);
        break;
      }
    }
    return new Context(this.exprFactory.cat(work), tokens);
  }

  private static String head(List<String> tokens) {
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.get(0);
  }

  private static List<String> tail(List<String> tokens) {
    return tokens.subList(1, tokens.size());
  }

  static class Context {
    static final Context LAST = new Context(null, null);
    final Expr         expr;
    final List<String> tokens;

    Context(Expr expr, List<String> tokens) {
      this.expr = expr;
      this.tokens = tokens;
    }

    boolean hasNext() {
      return this.expr != null;
    }
  }
}
