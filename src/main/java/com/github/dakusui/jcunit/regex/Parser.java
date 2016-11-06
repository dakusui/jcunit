package com.github.dakusui.jcunit.regex;

import com.github.dakusui.jcunit.core.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit.core.utils.Utils.transform;
import static java.util.Arrays.asList;

public class Parser {

  public static final Pattern QUANTIFIER_PATTERN = Pattern.compile("^\\{([0-9]+),([0-9]+)\\}");
  public static final Pattern LEAF_PATTERN       = Pattern.compile("^[A-Za-z_ ][A-Za-z_0-9 ]*");

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

  public static void main(String... args) {
    System.out.println(Parser.parse("(Hello|hello)world{0,1}everyone"));
    System.out.println(Parser.parse("(Hello|hello){0,1}world everyone"));
    System.out.println(Parser.parse("(Hello|hello)world everyone{0,1}"));
    System.out.println(Parser.parse("(Hello{0,11}|hello)world everyone"));
    System.out.println(Parser.parse("(Hello|hello{100,129})world everyone"));
    System.out.println(Parser.parse("(Hello world){100,129}world everyone"));
  }


  public static Expr parse(String regex) {
    return parse(tokenize(regex));
  }

  private static List<String> tokenize(String input) {
    List<String> ret = new LinkedList<String>();
    for (String[] cur = nextToken(input); cur != null; cur = nextToken(cur[1])) {
      if ("|".equals(cur[0]))
        continue;
      ret.add(cur[0]);
    }
    return ret;
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

  private static Expr parse(List<String> tokens) {
    Context result = readCat(tokens);
    checkcond(result.tokens.isEmpty(), "Syntax error: unparsed=%s", result.tokens);
    return result.expr;
  }

  private static Context readTerm(List<String> tokens) {
    String head = head(tokens);
    checkcond(!"|".equals(head), "Syntax error: head='%s'; tokens='%s'", head, tokens);
    Context ret;
    if ("(".equals(head)) {
      ret = readAlt(tail(tokens));
    } else {
      ret = readLeaves(tokens);
    }
    String nextHead;
    if (ret.tokens != null && (nextHead = head(ret.tokens)) != null) {
      Matcher m;
      if ((m = QUANTIFIER_PATTERN.matcher(nextHead)).find()) {
        ret = new Context(
            new Expr.Rep(ret.expr, Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
            tail(ret.tokens));
      }
    }
    return ret;
  }

  private static Context readLeaves(List<String> tokens) {
    String head = head(tokens);
    if (head == null)
      return Context.LAST;
    checkcond(!"(".equals(head), "Syntax error: head='%s', tokens='%s'", head, tokens);
    checkcond(!")".equals(head), "Syntax error: head='%s', tokens='%s'", head, tokens);
    List<Expr> work = transform(asList(head.split(" +")), new Utils.Form<String, Expr>() {
      @Override
      public Expr apply(String in) {
        return new Expr.Leaf(in);
      }
    });
    if (work.size() == 1) {
      return new Context(work.get(0), tail(tokens));
    }
    return new Context(new Expr.Cat(work), tail(tokens));
  }

  private static Context readAlt(List<String> tokens) {
    List<Expr> work = new LinkedList<Expr>();
    for (Context context = readTerm(tokens); context.hasNext(); context = readTerm(tokens)) {
      tokens = context.tokens;
      work.add(context.expr);
      if (")".equals(head(tokens))) {
        tokens = tail(tokens);
        break;
      }
    }
    if (work.size() == 1) {
      return new Context(work.get(0), tokens);
    }
    return new Context(new Expr.Alt(work), tokens);
  }

  private static Context readCat(List<String> tokens) {
    List<Expr> work = new LinkedList<Expr>();
    for (Context context = readTerm(tokens); context.hasNext(); context = readTerm(tokens)) {
      work.add(context.expr);
      tokens = context.tokens;
    }
    return new Context(new Expr.Cat(work), tokens);
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
}
