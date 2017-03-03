package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.utils.Utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Utils.filter;
import static com.github.dakusui.jcunit.core.utils.Utils.transform;
import static com.github.dakusui.jcunit.regex.Parser.Type.ALT;
import static com.github.dakusui.jcunit.regex.Parser.Type.CAT;
import static java.util.Arrays.asList;

public class Parser {
  public static final Pattern QUANTIFIER_PATTERN = Pattern.compile("^\\{([0-9]+),([0-9]+)\\}");
  public static final Pattern LEAF_PATTERN       = Pattern.compile("^[A-Za-z_ ][A-Za-z_0-9 ]*");
  private final Expr.Factory exprFactory;

  public Parser() {
    this.exprFactory = new Expr.Factory();
  }

  public Expr parse(String regex) {
    return parse(interpret(regex));
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

  public static List<String> interpret(String input) {
    List<String> ret = new LinkedList<String>();
    interpret(ret, tokenizer(input));
    return ret;
  }

  private static void interpret(List<String> output, Iterator<String> input) {
    List<String> work = new LinkedList<String>();
    Type type = CAT;
    try {
      while (input.hasNext()) {
        String cur = input.next();
        if ("|".equals(cur)) {
          type = ALT;
          continue;
        }
        if ("(".equals(cur)) {
          interpret(work, input);
          continue;
        }
        if (")".equals(cur)) {
          return;
        }
        work.add(cur);
      }
    } finally {
      work.add(0, type.toString());
      work.add(")");
      output.addAll(work);
    }
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
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }
    {
      Matcher m = QUANTIFIER_PATTERN.matcher(input);
      if (m.find()) {
        return new String[] { m.group(0), input.substring(m.group(0).length()) };
      }
    }

    throw new RuntimeException(String.format("Syntax error: Unparsable: '%s'", input));
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
      ret = readLeaves(tokens);
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

  private Context readLeaves(List<String> tokens) {
    String head = head(tokens);
    if (head == null)
      return Context.LAST;
    List<Expr> work = transform(
        filter(
            asList(head.split(" +")),
            new Utils.Predicate<String>() {
              @Override
              public boolean apply(String in) {
                return !"".equals(in);
              }
            }
        ), new Utils.Form<String, Expr>() {
          @Override
          public Expr apply(String in) {
            return exprFactory.leaf(in);
          }
        });
    if (work.size() == 1) {
      return new Context(work.get(0), tail(tokens));
    }
    return new Context(this.exprFactory.cat(work), tail(tokens));
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
    public static final Context LAST = new Context(null, null);
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
