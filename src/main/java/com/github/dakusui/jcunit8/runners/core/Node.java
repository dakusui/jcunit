package com.github.dakusui.jcunit8.runners.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface Node {
  void accept(Visitor visitor);

  interface Visitor {
    void visitLeaf(Leaf leaf);

    void visitAnd(And and);

    void visitOr(Or or);

    void visitNot(Not not);

    abstract class Base implements Visitor {
      @Override
      public void visitAnd(And and) {
        and.children().forEach(node -> node.accept(Base.this));
      }

      @Override
      public void visitOr(Or or) {
        or.children().forEach(node -> node.accept(Base.this));
      }

      @Override
      public void visitNot(Not not) {
        not.target().accept(this);
      }
    }
  }

  interface Leaf extends Node {
    String id();
    String[] args();

    class Impl implements Leaf {
      private static final String[] NONE = new String[0];
      private final String   id;
      private final String[] args;

      Impl(String s) {
        this.id = head(s);
        this.args = tail(s);
      }

      @Override
      public String id() {
        return id;
      }

      @Override
      public String[] args() {
        return this.args;
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visitLeaf(this);
      }

      private static String head(String s) {
        return s.contains(" ") ?
            s.substring(0, s.indexOf(' ')) :
            s;
      }

      private static String[] tail(String s) {
        return s.contains(" ") ?
            s.substring(s.indexOf(' ') + 1).split(" ") :
            NONE;
      }
    }
  }

  interface Not extends Node {
    Node target();

    class Impl implements Not {

      private final Node target;

      public Impl(Node target) {
        this.target = target;
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visitNot(this);
      }

      @Override
      public Node target() {
        return target;
      }
    }
  }

  interface Internal extends Node {
    List<Node> children();

    abstract class Base implements Internal {
      private final List<Node> children;

      Base(List<Node> nodes) {
        this.children = new ArrayList<>(nodes);
      }

      @Override
      public List<Node> children() {
        return Collections.unmodifiableList(this.children);
      }
    }
  }

  interface And extends Internal {
    class Impl extends Base implements And {
      Impl(List<Node> nodes) {
        super(nodes);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visitAnd(this);
      }
    }
  }

  interface Or extends Internal {
    class Impl extends Base implements Or {
      Impl(List<Node> nodes) {
        super(nodes);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visitOr(this);
      }
    }
  }
}
