package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.regex.Parser.Type.ALT;
import static com.github.dakusui.jcunit.regex.Parser.Type.CAT;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class Parser {
  private static final Pattern QUANTIFIER_PATTERN = Pattern.compile("^\\{([0-9]+),([0-9]+)}");
  /*
   * We can implement the same mechanism by considering a white space a 'concatenation operator',
   * but it increases number of factors and constraints generated. And it results
   * in poorer performance.
   * Instead, treat white spaces within a word just as part of the word, and after reverse
   * regex generation finishes, JCUnit will split into pieces.
   *
   * See RegexComposer, too.
   */
  private static final Pattern LEAF_PATTERN       = Pattern.compile("^[A-Za-z_]([A-Za-z_0-9 ]*[A-Za-z_0-9])?");
  private final Expr.Factory exprFactory;

  public Parser() {
    this.exprFactory = new Expr.Factory();
  }

  public Expr parse(String regex) {
    return parse(preprocess(regex));
  }

  enum Type {
    CAT("*"),
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

  public static List<String> preprocess(String input) {
    List<String> ret = new LinkedList<String>();
    List<String> read = new LinkedList<String>();
    preprocess(read, ret, tokenizer(input), true);
    return ret;
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

  private static void preprocess(List<String> read, List<String> output, Iterator<String> input, boolean topLevel) {
    Type type = null;
    List<String> work = new LinkedList<String>();
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
            StringUtils.join("", work.subList(0, Math.max(0, work.size() - 1))))
    );
  }

  private static RuntimeException inputShouldNotEndHere(PreprocessingState state) {
    throw new InvalidTestException(format("Input should not end here: '%s'", state));
  }

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

  private static String[] nextToken(String input) {
    if (input.isEmpty())
      return null;
    char first = input.charAt(0);
    if (first == '(' || first == ')' || first == '|') {
      return new String[] { input.substring(0, 1), input.substring(1) };
    }
    {
      Matcher m = LEAF_PATTERN.matcher(input);
      if (m.find()) {
        /*
        String matchedPart = m.group(0);
        String work = matchedPart.contains(" ") ?
            matchedPart.substring(0, matchedPart.indexOf(" ")) :
            matchedPart;
        return new String[] { work, input.substring(work.length()).trim() };
        */
        //return new String[] { m.group(0), input.substring(m.group(0).length()).trim() };
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }
    {
      Matcher m = QUANTIFIER_PATTERN.matcher(input);
      if (m.find()) {
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }

    throw new InvalidTestException(format("Syntax error: Unparsable: '%s' did neither match '%s' nor '%s'", input, LEAF_PATTERN, QUANTIFIER_PATTERN));
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
